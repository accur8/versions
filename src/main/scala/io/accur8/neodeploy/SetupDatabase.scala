package io.accur8.neodeploy


import a8.shared.jdbcf.{Conn, ConnFactory, DatabaseConfig, SchemaName, SqlString}
import io.accur8.neodeploy.model.{DatabaseName, DatabaseUserDescriptor, DatabaseUserRole}
import SharedImports.*
import VFileSystem.Directory
import a8.shared.jdbcf.DatabaseConfig.Password
import a8.versions.model.ResolvedRepo
import io.accur8.neodeploy.SetupDatabase.*
import io.accur8.neodeploy.systemstate.SystemStateModel
import io.accur8.neodeploy.systemstate.SystemStateModel.PathLocator
import model.*

import scala.jdk.CollectionConverters.given
import java.nio.charset.StandardCharsets

object SetupDatabase extends LoggingF {

  type Env = PathLocator & zio.Scope
  type Z[A] = zio.ZIO[Env, Throwable, A]

  implicit class StringOps(val value: String) extends AnyVal {

    def stripPrefix(prefix: String): String =
      if ( value.startsWith(prefix) )
        value.substring(prefix.length)
      else
        value

    def stripSuffix(suffix: String): String =
      if ( value.endsWith(suffix) )
        value.substring(0, value.length - suffix.length)
      else
        value

  }

  case class OwnerConn(value: Conn, config: DatabaseConfig)
  case class SuperUserConn(value: Conn, config: DatabaseConfig)
  case class Secrets(value: Map[String,String]) {
    lazy val databaseUserPasswords: Map[UserLogin, Password] =
      value
        .filter(_._1.startsWith(impl.passwordPropertyPrefix))
        .map { (k,v) =>
          val user = UserLogin(k.stripPrefix(impl.passwordPropertyPrefix).stripSuffix(impl.passwordPropertySuffix))
          val password = Password(v)
          (user, password)
        }
  }

  object impl {

    val secretsFilename = "secrets.hocon.priv"

    val rand = new scala.util.Random(new java.util.Random())
    val allowedPasswordCharacters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes

    def mkStr(chars: Array[Byte], length: Int): String = {
      val bytes = new Array[Byte](length)
      for (i <- 0 until length) bytes(i) = chars(rand.nextInt(chars.length))
      new String(bytes, StandardCharsets.US_ASCII)
    }

    // database url
    // database user
    // database password

    val passwordPropertyPrefix = "secrets.database.users."
    val passwordPropertySuffix = ".password"

    def nextAlphanumeric(length: Int): String = mkStr(allowedPasswordCharacters, length)

    def parseProperties(contents: Option[String]): Map[String,String] = {
      val props = new java.util.Properties
      props
        .load(new java.io.StringReader(contents.getOrElse("")))
      props.asScala.toMap
    }

    def loadSecrets(directory: Directory): Z[Secrets] = {
      val secretsFile = directory.file(secretsFilename)
      for {
        contents <- secretsFile.readAsStringOpt
      } yield Secrets(parseProperties(contents))
    }

    def randomPassword(): Password = {
      Password(impl.nextAlphanumeric(20))
    }

    def updateSecretsWithPasswords(users: Iterable[UserLogin], inputSecrets: Secrets): Secrets = {
      val map: Map[String, String] =
        users
          .foldLeft(inputSecrets.value) { (secretsMap, user) =>
            val key: String = passwordPropertyPrefix + user.value + passwordPropertySuffix
            val newMap: Map[String,String] =
              secretsMap.get(key) match {
                case None =>
                  val password = impl.randomPassword()
                  secretsMap + (key -> password.value)
                case Some(value) =>
                  secretsMap
              }
            newMap
          }
      Secrets(map)
    }

    def writeSecrets(secrets: Secrets, directory: Directory) = {
      val secretsFile = directory.file(secretsFilename)
      val contents =
        secrets
          .value
          .map { case (k,v) =>
            s"""${k} = ${v}"""
          }
          .mkString("\n")
      secretsFile.write(contents)
    }

  }

  def loadOrGeneratePasswords(users: Iterable[UserLogin], gitAppDirectory: Directory): Z[Map[UserLogin, Password]] = {
    import impl._
    loadSecrets(gitAppDirectory)
      .map(updateSecretsWithPasswords(users, _))
      .tap(secrets => writeSecrets(secrets, gitAppDirectory))
      .map(_.databaseUserPasswords)
  }

  def setupDatabases(resolvedRepo: resolvedmodel.ResolvedRepository, resolvedDeployArgs: ResolvedDeployArgs): Z[Unit] =
    resolvedDeployArgs
      .args
      .collect {
        case ad: AppDeploy =>
          ad.resolvedApp.descriptor.setup.database.map(ad -> _)
      }
      .flatten
      .map { case (ad, databaseSetupDescriptor) =>
        for {
          passwords <- loadOrGeneratePasswords(databaseSetupDescriptor.allUsers, ad.resolvedApp.gitDirectory)
          _ <-
            SetupDatabase(
              databaseSetupDescriptor,
              resolvedRepo.gitRootDirectory,
              ad.resolvedApp.gitDirectory,
              passwords,
            ).run
        } yield ()
      }
      .sequence
      .as(())

  def updateSecretsWithDatabaseConfig(gitAppDirectory: Directory, databaseConfig: DatabaseConfig): Z[Unit] = {
    import impl._
    loadSecrets(gitAppDirectory)
      .map { secrets =>
        val newMap: Map[String,String] =
          (secrets.value ++ Map(
            "secrets.database.url" -> databaseConfig.url.toString,
            "secrets.database.user" -> databaseConfig.user,
            "secrets.database.password" -> databaseConfig.password.value,
          ))
        Secrets(newMap)
      }
      .tap(secrets => writeSecrets(secrets, gitAppDirectory))
      .as(())
  }

}

case class SetupDatabase(
//  qubesServer: DomainName,
  databaseSetupDescriptor: DatabaseSetupDescriptor,
  gitRootDirectory: GitRootDirectory,
  gitAppDirectory: Directory,
  passwords: Map[UserLogin,Password],
) {

  lazy val ownerUser = databaseSetupDescriptor.owner
  lazy val schemaName: SchemaName = SchemaName("public")
  lazy val databaseName: DatabaseName = databaseSetupDescriptor.databaseName

  def configM(serverName: DomainName): Z[DatabaseConfig] = {
    val file =
      gitRootDirectory
        .unresolved
        .file("database-configs.json.priv")
    file
      .readAsString
      .flatMap(json.readF[Iterable[DatabaseConfig]](_))
      .flatMap { configs =>
        configs.find(_.id.value.toString == serverName.value) match {
          case None =>
            zfail(new RuntimeException(s"cannot find database ${serverName} in ${file}"))
          case Some(db) =>
            zsucceed(db)
        }
      }
  }

  def password(userDescriptor: DatabaseUserDescriptor): Password =
    passwords(userDescriptor.name)

  def connectM(server: DomainName): Z[(Conn,DatabaseConfig)] =
    for {
      databaseConfig <- configM(server)
      conn <- Conn.fromNewConnection(databaseConfig.url, databaseConfig.user, databaseConfig.password.value)
    } yield (conn, databaseConfig)

  lazy val superUserConnM: Z[SuperUserConn] =
    connectM(databaseSetupDescriptor.databaseServer)
      .map(t => SuperUserConn(t._1, t._2))

  lazy val ownerDatabaseConfigM: Z[DatabaseConfig] =
    configM(databaseSetupDescriptor.databaseServer)
      .map { superUserConfig =>
        val uri = superUserConfig.url
        val newJdbcUri = uri.withPath(uri.path.take(uri.path.length - 1) ++ Some(databaseName.value))
        superUserConfig
          .copy(
            url = newJdbcUri,
            user = ownerUser.name.value,
            password = password(ownerUser),
          )
      }

  lazy val ownerConnM: Z[OwnerConn] =
    ownerDatabaseConfigM
      .flatMap { config =>
        Conn
          .fromNewConnection(
            config.url,
            config.user,
            config.password.value,
          )
          .map(conn => OwnerConn(conn, config))
      }

  def run: Z[Unit] = {
    for {
      superUserConn <- superUserConnM
      _ <- createDatabase(superUserConn)
      ownerConn <- ownerConnM
      _ <- SetupDatabase.updateSecretsWithDatabaseConfig(gitAppDirectory, ownerConn.config)
      _ <- databaseSetupDescriptor.extraUsers.map(createUser(_)(ownerConn, superUserConn)).sequence
//      _ <- databaseSetupDescriptor.zooFiles.map(createTablesAndQubes(_, ownerConn)).sequence
    } yield ()
  }

  def createTablesAndQubes(zooFile: ZooFile, ownerConn: OwnerConn): Z[Unit] = {
    // Step 0 - load the qubes and main database configs
    // Step 1 - get the zoo files
    // Step 2 - run zoo
//    ???
    zunit
  }

  def createUser(user: DatabaseUserDescriptor)(implicit ownerConn: OwnerConn, superUserConn: SuperUserConn): Z[Unit] = {
    if ( user.roles.contains(DatabaseUserRole("write")) ) {
      runSuperUserSql(
        z"DROP USER IF EXISTS ${user.name}",
        z"CREATE USER ${user.name} WITH ENCRYPTED PASSWORD '${password(user).value}'",
      ) *>
      runUpdateSql(
        z"GRANT CONNECT ON DATABASE ${databaseName} TO ${user.name}",
        z"GRANT USAGE ON SCHEMA ${schemaName} TO ${user.name}",
        z"GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE ON ALL TABLES IN SCHEMA ${schemaName} TO ${user.name}",
        z"GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA ${schemaName} TO ${user.name}",
        z"GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ${schemaName} TO ${user.name}",
        z"ALTER DEFAULT PRIVILEGES IN SCHEMA ${schemaName} GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE ON TABLES TO ${user.name}",
        z"ALTER DEFAULT PRIVILEGES IN SCHEMA ${schemaName} GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO ${user.name}",
        z"ALTER DEFAULT PRIVILEGES IN SCHEMA ${schemaName} GRANT EXECUTE ON FUNCTIONS TO ${user.name}",
      )
    } else if ( user.roles.contains(DatabaseUserRole("read")) ) {
      runSuperUserSql(
        z"DROP USER IF EXISTS ${user.name}",
        z"CREATE USER ${user.name} WITH ENCRYPTED PASSWORD '${password(user).value}'",
      ) *>
      runUpdateSql(
        z"GRANT CONNECT ON DATABASE ${databaseName} TO ${user.name}",
        z"GRANT USAGE ON SCHEMA ${schemaName} TO ${user.name}",
        z"GRANT SELECT ON ALL TABLES IN SCHEMA ${schemaName} TO ${user.name}",
        z"GRANT SELECT ON ALL SEQUENCES IN SCHEMA ${schemaName} TO ${user.name}",
        z"GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA ${schemaName} TO ${user.name}",
        z"ALTER DEFAULT PRIVILEGES IN SCHEMA ${schemaName} GRANT SELECT ON TABLES TO ${user.name}",
        z"ALTER DEFAULT PRIVILEGES IN SCHEMA ${schemaName} GRANT SELECT ON SEQUENCES TO ${user.name}",
        z"ALTER DEFAULT PRIVILEGES IN SCHEMA ${schemaName} GRANT EXECUTE ON FUNCTIONS TO ${user.name}",
      )
    } else {
      zfail(new RuntimeException(z"""Unknown roles for user ${user.name} ${user.roles.mkString(" ")}"""))
    }
  }

  def createDatabase(implicit superUserConn: SuperUserConn): Z[Unit] = {
    runSuperUserSql(
      z"DROP USER IF EXISTS ${ownerUser.name}",
      z"CREATE USER ${ownerUser.name} WITH ENCRYPTED PASSWORD '${password(ownerUser).value}'",
      z"CREATE DATABASE ${databaseSetupDescriptor.databaseName} OWNER = ${ownerUser.name}",
    )
  }

  def runSuperUserSql(sql: String*)(implicit superUserConn: SuperUserConn): Z[Unit] = {
    val conn = superUserConn.value
    sql
      .map { s =>
        val sqlStr = SqlString.RawSqlString(s)
        conn.update(sqlStr)
      }
      .sequence
      .as(())
  }

  def runUpdateSql(sql: String*)(implicit ownerConn: OwnerConn): Z[Unit] = {
    val conn = ownerConn.value
    sql
      .map { s =>
        val sqlStr = SqlString.RawSqlString(s)
        conn.update(sqlStr)
      }
      .sequence
      .as(())
  }

}
