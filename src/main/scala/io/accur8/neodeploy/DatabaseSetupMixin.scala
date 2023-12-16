package io.accur8.neodeploy


import a8.shared.jdbcf.{Conn, ConnFactory, DatabaseConfig, SchemaName, SqlString}
import io.accur8.neodeploy.model.{DatabaseName, DatabaseUserDescriptor, DatabaseUserRole}
import SharedImports.*
import VFileSystem.Directory
import a8.shared.CompanionGen
import a8.shared.jdbcf.DatabaseConfig.Password
import a8.versions.model.ResolvedRepo
import io.accur8.neodeploy.DatabaseSetupMixin.{loggerF as _, *}
import io.accur8.neodeploy.LocalDeploy.Config
import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedRepository}
import io.accur8.neodeploy.systemstate.{SystemState, SystemStateModel}
import io.accur8.neodeploy.systemstate.SystemStateModel.{PathLocator, StateKey}
import model.{loggerF as _, *}

import scala.jdk.CollectionConverters.given
import java.nio.charset.StandardCharsets

object DatabaseSetupMixin extends LoggingF {

  type Z[A] = SystemStateModel.M[A]

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
  case class GeneratedProperties(value: Map[String,String]) {
    lazy val databasePasswords: Passwords =
      Passwords(
        value
          .filter(_._1.startsWith(impl.passwordPropertyPrefix))
          .map { (k,v) =>
            val user = UserLogin(k.stripPrefix(impl.passwordPropertyPrefix).stripSuffix(impl.passwordPropertySuffix))
            UserPassword(user, v)
          }
          .toVector
      )
  }

  object impl {

    val generatedPropertiesFilename = "generated.properties"

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

    val passwordPropertyPrefix = "database.users."
    val passwordPropertySuffix = ".password"

    def nextAlphanumeric(length: Int): String = mkStr(allowedPasswordCharacters, length)

    def parseProperties(contents: Option[String]): Map[String,String] = {
      val props = new java.util.Properties
      props
        .load(new java.io.StringReader(contents.getOrElse("")))
      props.asScala.toMap
    }

    def loadGeneratedProperties(directory: Directory): Z[GeneratedProperties] = {
      val file = directory.file(generatedPropertiesFilename)
      for {
        contents <- file.readAsStringOpt
      } yield GeneratedProperties(parseProperties(contents))
    }

    def randomPassword(): Password = {
      Password(impl.nextAlphanumeric(20))
    }

    def updatePropertiesWithPasswords(users: Iterable[UserLogin], inputProps: GeneratedProperties): GeneratedProperties = {
      val map: Map[String, String] =
        users
          .foldLeft(inputProps.value) { (secretsMap, user) =>
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
      GeneratedProperties(map)
    }

    def writeSecrets(secrets: GeneratedProperties, directory: Directory) = {
      val file = directory.file(generatedPropertiesFilename)
      val contents =
        secrets
          .value
          .map { case (k,v) =>
            s"""${k} = ${v}"""
          }
          .mkString("\n")
      file.write(contents)
    }

    def loadOrGeneratePasswords(users: Iterable[UserLogin], gitAppDirectory: Directory): Z[Passwords] = {
      loadGeneratedProperties(gitAppDirectory)
        .map(updatePropertiesWithPasswords(users, _))
        .tap(secrets => writeSecrets(secrets, gitAppDirectory))
        .map(_.databasePasswords)
    }

    def updateGeneratedPropertiesWithDatabaseConfig(gitAppDirectory: Directory, databaseConfig: DatabaseConfig): Z[GeneratedProperties] = {
      import impl._
      loadGeneratedProperties(gitAppDirectory)
        .map { secrets =>
          val newMap: Map[String, String] =
            (secrets.value ++ Map(
              "database.url" -> databaseConfig.url.toString,
              "database.user" -> databaseConfig.user,
              "database.password" -> databaseConfig.password.value,
            ))
          GeneratedProperties(newMap)
        }
        .tap(secrets => writeSecrets(secrets, gitAppDirectory))
    }

    def superUserconfigM(serverName: DomainName): M[DatabaseConfig] = {
      zservice[Config]
        .map(_.gitRootDirectory)
        .flatMap { gitRootDirectory =>
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
    }

  }

  def loadOrGenerateProperties(databaseSetup: DatabaseSetupDescriptor, gitAppDirectory: Directory): Z[GeneratedProperties] = {
    for {
      passwords <-
        impl.loadOrGeneratePasswords(
          databaseSetup.allUsers,
          gitAppDirectory,
        )
      config <- databaseConfigM(databaseSetup.databaseServer, databaseSetup.databaseName, passwords.credentials(databaseSetup.owner))
      generatedProps <- impl.updateGeneratedPropertiesWithDatabaseConfig(gitAppDirectory, config)
    } yield generatedProps
  }

  def databaseConfigM(serverName: DomainName, databaseName: DatabaseName, credentials: UserPassword): Z[DatabaseConfig] =
    impl.superUserconfigM(serverName)
      .map { superUserConfig =>
        val uri = superUserConfig.url
        val newJdbcUri = uri.withPath(uri.path.take(uri.path.length - 1) ++ Some(databaseName.value))
        superUserConfig
          .copy(
            url = newJdbcUri,
            user = credentials.user.value,
            password = credentials.password,
          )
      }

}

trait DatabaseSetupMixin extends LoggingF { self: SystemState.DatabaseSetup =>

  lazy val ownerUser = owner
  lazy val schemaName: SchemaName = SchemaName("public")

  override def dryRunInstall: Vector[String] =
    Vector(
      z"setup database ${databaseName} on server ${databaseServer}"
    )

  def gitRootDirectoryM: M[GitRootDirectory] =
    zservice[ResolvedRepository]
      .map(_.gitRootDirectory)

  def password(userDescriptor: DatabaseUserDescriptor): Password =
    passwords(userDescriptor)

  def password(user: UserLogin): Password =
    passwords(user)

  def connectM(server: DomainName): Z[(Conn,DatabaseConfig)] =
    for {
      databaseConfig <- impl.superUserconfigM(server)
      conn <- Conn.fromNewConnection(databaseConfig.url, databaseConfig.user, databaseConfig.password.value)
    } yield (conn, databaseConfig)

  lazy val superUserConnM: Z[SuperUserConn] =
    connectM(databaseServer)
      .map(t => SuperUserConn(t._1, t._2))

  lazy val ownerDatabaseConfigM: Z[DatabaseConfig] =
    databaseConfigM(databaseServer, databaseName, passwords.credentials(owner))

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

  override def isActionNeeded: M[Boolean] =
    for {
      superUserConn <- superUserConnM
      databaseExists <- databaseExists(superUserConn)
    } yield !databaseExists

  override def runApplyNewState: M[Unit] = {
    for {
      superUserConn <- superUserConnM
      databaseExists <- databaseExists(superUserConn)
      _ <-
        if ( databaseExists ) {
          loggerF.info(s"database ${databaseName} on server ${databaseServer} already exists no more setup")
        } else {
          for {
            _ <- createDatabase(superUserConn)
            ownerConn <- ownerConnM
            _ <- extraUsers.map(createUser(_)(ownerConn, superUserConn)).sequence
            //      _ <- databaseSetupDescriptor.zooFiles.map(createTablesAndQubes(_, ownerConn)).sequence
          } yield ()
        }
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
      z"DROP USER IF EXISTS ${ownerUser}",
      z"CREATE USER ${ownerUser} WITH ENCRYPTED PASSWORD '${password(ownerUser).value}'",
      z"CREATE DATABASE ${databaseName} OWNER = ${ownerUser}",
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

  def databaseExists(superUserConn: SuperUserConn): Z[Boolean] = {
    val conn = superUserConn.value
    val sql = SqlString.RawSqlString(s"""select exists(SELECT 1 FROM pg_database WHERE datname = '${databaseName.value}')""")
    conn.query[Boolean](sql).fetch
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

  override def stateKey: Option[io.accur8.neodeploy.systemstate.SystemStateModel.StateKey] =
    Some(StateKey("database", z"${databaseServer}/${databaseName}"))

  override def runUninstallObsolete(interpreter: io.accur8.neodeploy.systemstate.Interpreter): M[Unit] =
    zunit

}
