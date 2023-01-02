package io.accur8.neodeploy


import a8.shared.{CascadingHocon, CompanionGen, ConfigMojo, ConfigMojoOps, Exec, HoconOps, StringValue, ZString}
import a8.shared.ZFileSystem.{Directory, File, dir, userHome}
import model._
import a8.shared.SharedImports._
import a8.shared.ZString.ZStringer
import a8.shared.app.LoggingF
import a8.shared.json.{JsonCodec, JsonReader}
import a8.shared.json.ast.{JsDoc, JsNothing, JsObj, JsVal}
import io.accur8.neodeploy.Sync.SyncName
import zio.{Chunk, Task, UIO, ZIO}
import PredefAssist._
import a8.shared.json.JsonReader.ReadResult
import com.typesafe.config.{Config, ConfigFactory, ConfigValue}
import io.accur8.neodeploy.plugin.{PgbackrestServerPlugin, RepositoryPlugins}
import io.accur8.neodeploy.systemstate.SystemStateModel.{Environ, M}

object resolvedmodel extends LoggingF {

  case class ResolvedUser(
    descriptor: UserDescriptor,
    home: Directory,
    server: ResolvedServer,
  ) {

    def resolveLoginKeys: Task[Vector[ResolvedAuthorizedKey]] =
      publicKey
        .flatMap {
          case Some(pk) =>
            zsucceed(Vector(ResolvedAuthorizedKey(qualifiedUserName.value, pk.asAuthorizedKey)))
          case None =>
            server.repository.publicKeys(qualifiedUserName)
        }

    lazy val gitAppsDirectory =
      server.gitServerDirectory.subdir(descriptor.login.value)

    lazy val resolvedAppsT: Task[Vector[ResolvedApp]] =
      gitAppsDirectory
        .subdirs
        .flatMap(
          _.map(appDir => server.loadResolvedAppFromDisk(appDir, this))
            .sequence
            .map(_.flatten.toVector)
        )

    lazy val plugins = UserPlugin.UserPlugins(this)

    lazy val a8VersionsExec =
      descriptor
        .a8VersionsExec
        .orElse(server.descriptor.a8VersionsExec)
        .getOrElse("/usr/bin/a8-versions")

    lazy val appsRootDirectory: AppsRootDirectory =
      descriptor
        .appInstallDirectory
        .getOrElse(AppsRootDirectory(home.subdir("apps").absolutePath))

    lazy val qualifiedUserNames: Seq[QualifiedUserName] =
      Vector(qualifiedUserName) ++ descriptor.aliases

    lazy val qualifiedUserName: QualifiedUserName =
      QualifiedUserName(qname)

    def qname = z"${login}@${server.name}"

    def sshName = z"${login}@${server.descriptor.vpnDomainName}"

    def login = descriptor.login

    lazy val repoDir =
      server
        .gitServerDirectory
        .subdir(descriptor.login.value)

    def repoFile(subPath: String): File =
      repoDir
        .file(subPath)

    def tempSshPrivateKeyFileInRepo =
      repoFile("id_ed25519")

    def sshPrivateKeyFileInRepo =
      repoFile(z"id_ed25519.priv")

    def sshPublicKeyFileInRepo =
      repoFile(z"id_ed25519.pub")

    def sshPrivateKeyFileInHome =
      home.subdir(".ssh").file("id_ed25519")

    def sshPublicKeyFileInHome =
      home.subdir(".ssh").file("id_ed25519.pub")

    def publicKey: Task[Option[PublicKey]] =
      sshPublicKeyFileInRepo
        .readAsStringOpt
        .map(
          _.map(line => PublicKey(line))
        )

    def resolvedAuthorizedKeys(stack: Set[QualifiedUserName]): Task[Vector[ResolvedAuthorizedKey]] = {
      if ( stack(qualifiedUserName) ) {
        zsucceed(Vector.empty)
      } else {
        val newStack = stack + qualifiedUserName
        for {
          pak <- plugins.resolveAuthorizedKeys
          dak <-
            descriptor
              .authorizedKeys
              .map(id => server.repository.resolvedAuthorizedKeys(id, newStack))
              .sequence
              .map(_.flatten)
        } yield pak ++ dak
      }
    }

  }


  case class ResolvedServer(
    descriptor: ServerDescriptor,
    gitServerDirectory: GitServerDirectory,
    repository: ResolvedRepository,
  ) {

    def virtualHosts = {
      resolvedUsers
        .map(_.resolvedAppsT)
        .sequence
        .map(_.flatten)
        .map { apps =>
          def impl(serverNameOpt: Option[DomainName]) =
            serverNameOpt
              .toVector
              .flatMap { serverName =>
                val tld = serverName.topLevelDomain
                apps
                  .flatMap { app =>
                    app.descriptor.listenPort.map { listenPort =>
                      VirtualHost(
                        serverName = serverName,
                        virtualNames = app.descriptor.resolvedDomainNames.filter(_.isSubDomainOf(tld)),
                        listenPort = listenPort,
                      )
                    }
                  }
              }
          impl(descriptor.publicDomainName) ++ impl(descriptor.vpnDomainName.some)
        }

    }

    def fetchUserZ(login: UserLogin): ZIO[Any, Throwable, ResolvedUser] =
      fetchUserOpt(login)
        .map(u => zsucceed(u))
        .getOrElse(zfail(new RuntimeException(z"user ${login} not found")))

    def fetchUserOpt(login: UserLogin): Option[ResolvedUser] =
      resolvedUsers
        .find(_.login == login)

    lazy val resolvedUsers =
      descriptor
        .users
        .map( userDescriptor =>
          ResolvedUser(
            descriptor = userDescriptor,
            home = userDescriptor.home.getOrElse(dir(z"/home/${userDescriptor.login}")),
            server = this,
          )
        )

    def supervisorCommand(action: String, applicationName: ApplicationName): Command =
      Command(Seq(
        descriptor.supervisorctlExec.getOrElse("supervisorctl"),
        action,
        applicationName.value
      ))

    def execCommand(command: Command): Task[Unit] = {
      val logLinesEffect: Chunk[String] => UIO[Unit] = { lines: Chunk[String] =>
        loggerF.debug(s"command output chunk -- ${lines.mkString("\n    ", "\n    ", "\n    ")}")
      }
      command
        .exec(logLinesEffect = logLinesEffect)
        .as(())
    }

    def name = descriptor.name

    def supervisorDirectory: SupervisorDirectory = descriptor.supervisorDirectory
    def caddyDirectory: CaddyDirectory = descriptor.caddyDirectory

    def loadResolvedAppFromDisk(appConfigDir: Directory, resolvedUser: ResolvedUser): Task[Option[ResolvedApp]] = {
      val appDescriptorFilesZ =
        Vector(
          appConfigDir.file("secret.props.priv"),
          appConfigDir.file("application.json"),
          appConfigDir.file("application.hocon"),
        )
          .map(f => f.exists.map(_ -> f))
          .sequence
          .map(
            _.collect {
              case (true, f) =>
                f
            }
          )

      val appDir = resolvedUser.appsRootDirectory.subdir(appConfigDir.name)


      val baseConfigMap =
        Map(
          "appDir" -> appDir.absolutePath,
          "dataDir" -> appDir.subdir("data").absolutePath,
        )

      val baseConfig = ConfigFactory.parseMap(baseConfigMap.asJava)

      appDescriptorFilesZ
        .flatMap { appDescriptorFiles =>
          try {
            import HoconOps._

            val configs =
              appDescriptorFiles
                .map(f => HoconOps.impl.loadConfig(f.asNioPath))
            if (configs.isEmpty) {
              zsucceed(None)
            } else {
              val resolvedConfig =
                (configs ++ Vector(baseConfig))
                  .reduceLeft(_.resolveWith(_))

              val readResult = JsonReader[ApplicationDescriptor].read(resolvedConfig)
              def logWarnings =
                if ( readResult.warnings.nonEmpty ) {
                  loggerF.warn(s"found the following warnings reading the applicationDescriptor from ${appDescriptorFiles.mkString(" ")}\n${readResult.warnings.mkString("\n").indent("    ")}")
                } else {
                  zunit
                }

              val effect =
                readResult match {
                  case ReadResult.Success(descriptor, _) =>
                    zsucceed(ResolvedApp(descriptor, appConfigDir, resolvedUser).some)
                  case ReadResult.Error(re, _) =>
                    loggerF.error(s"error reading application descriptor from ${appDescriptorFiles.mkString(" ")} -- ${re.prettyMessage}")
                      .as(None)
                }

              logWarnings *> effect

            }
          } catch {
            case IsNonFatal(e) =>
              loggerF.error(s"Failed to load application descriptor file: $appDescriptorFiles", e)
                .as(None)
          }

        }
    }
  }

  object ResolvedApp {
  }

  case class ResolvedApp(
    descriptor: ApplicationDescriptor,
    gitDirectory: Directory,
    user: ResolvedUser,
  ) {
    def server = user.server
    def name = descriptor.name
    def appDirectory = user.appsRootDirectory.subdir(descriptor.name.value)
  }


  object ResolvedRepository {

    def loadFromDisk(gitRootDirectory: GitRootDirectory): ResolvedRepository = {
      val cascadingHocon =
        CascadingHocon
          .loadConfigsInDirectory(gitRootDirectory.asNioPath, recurse = false)
          .resolve
      val configMojo =
        ConfigMojoOps.impl.ConfigMojoRoot(
          cascadingHocon.config.root(),
          cascadingHocon,
        )
      val repositoryDescriptor = configMojo.as[RepositoryDescriptor]
      ResolvedRepository(repositoryDescriptor, gitRootDirectory)
    }

  }

  case class VirtualHost(
    serverName: DomainName,
    virtualNames: Vector[DomainName],
    listenPort: ListenPort,
  )

  case class ResolvedRepository(
    descriptor: RepositoryDescriptor,
    gitRootDirectory: GitRootDirectory,
  ) {


    /**
     * takes the domain name and if it is managed splits it out into the top level domain and the managed domain
     *
     * So if xyz.domain.com is the domainName passed in and domain.com is the managed domain then this returns
     * Some("xyz", DomainName("domain.com"))
     *
     */
    def findManagedDomain(domainName: DomainName): Option[ManagedDomain] = {
      val tld = domainName.topLevelDomain
      descriptor
        .managedDomains
        .find(_.topLevelDomains.contains(tld))
    }
    lazy val repositoryPlugins = RepositoryPlugins(this)

    def virtualHosts: Task[Vector[VirtualHost]] =
      servers
        .map(_.virtualHosts)
        .sequence
        .map(_.flatten)

    def applications: Task[Vector[ResolvedApp]] =
      users
        .map(_.resolvedAppsT)
        .sequence
        .map(_.flatten)

    def fetchUser(qname: QualifiedUserName): ResolvedUser =
      users
        .find(_.qualifiedUserName === qname)
        .getOrError(s"user ${qname} not found")


    lazy val userPlugins: Vector[UserPlugin] =
      for {
        server <- servers
        user <- server.resolvedUsers
        plugin <- user.plugins.pluginInstances
      } yield plugin

    def server(serverName: ServerName): ResolvedServer =
      servers
        .find(_.name == serverName)
        .getOrError(z"server ${serverName} not found")

    def publicKeys(id: QualifiedUserName) =
      gitRootDirectory
        .subdir("public-keys")
        .file(id.value)
        .readAsStringOpt
        .map {
          case Some(contents) =>
            val keys =
              contents
                .linesIterator
                .filterNot(_.isBlank)
                .map(AuthorizedKey.apply)
                .toVector
            Vector(
              ResolvedAuthorizedKey(
                s"public key ${id}",
                keys,
              )
            )
          case None =>
            Vector.empty
        }

    /**
     * authorized keys from personnel and public-keys folder in the rpo
     */
    def resolvedAuthorizedKeys(id: QualifiedUserName, stack: Set[QualifiedUserName]): Task[Vector[ResolvedAuthorizedKey]] = {

      if (stack(id)) {
        zsucceed(Vector.empty)
      } else {

        val stackWithId = stack + id

        def personnelFinderZ =
          personnel
            .find(_.id === id)
            .map(_.resolveKeys(stackWithId))
            .getOrElse(zsucceed(Vector.empty))

        for {
          personnelFinder <- personnelFinderZ
          publicKeys <- publicKeys(id)
        } yield {
          val result = personnelFinder ++ publicKeys

          result match {
            case v if v.nonEmpty =>
              v
            case _ =>
              logger.warn(s"unable to find keys for ${id}")
              Vector.empty
          }
        }

      }
    }

    lazy val resolvedPgbackrestServerOpt =
      userPlugins
        .collect {
          case ps: PgbackrestServerPlugin =>
            ps
        }
        .headOption

    lazy val personnel =
      descriptor
        .publicKeys
        .map { p =>
          ResolvedPersonnel(
            this,
            p,
          )
        }

    lazy val users =
      servers.flatMap(_.resolvedUsers)

    lazy val servers =
      descriptor
        .servers
        .map { serverDescriptor =>
          ResolvedServer(
            serverDescriptor,
            GitServerDirectory(gitRootDirectory.subdir(serverDescriptor.name.value).asNioPath.toString),
            this,
          )
        }

    lazy val allUsers: Seq[ResolvedUser] =
      servers
        .flatMap(_.resolvedUsers)

  }


  case class ResolvedPersonnel(
    repository: ResolvedRepository,
    descriptor: Personnel,
  ) {

    val id = descriptor.id

    def resolveKeys(stack: Set[QualifiedUserName]): Task[Vector[ResolvedAuthorizedKey]] = {

      val keysFromUrl: Seq[ResolvedAuthorizedKey] =
        descriptor
          .authorizedKeysUrl
          .toVector
          .map { url =>
            ResolvedAuthorizedKey(
              s"from ${url}",
              CodeBits.downloadKeys(url),
            )
          }

      val keysFromMembersZ: ZIO[Any, Throwable, Vector[ResolvedAuthorizedKey]] =
        descriptor
          .members
          .map(member =>
            repository.resolvedAuthorizedKeys(member, stack)
          )
          .sequence
          .map(_.flatten.toVector)

      keysFromMembersZ
        .map( keysFromMembers =>
          (descriptor.resolvedAuthorizedKeys ++ keysFromUrl ++ keysFromMembers)
            .map(_.withParent(id.value))
            .toVector
        )

    }
  }

  object ResolvedAuthorizedKey {

    def apply(source: String, keys: Vector[AuthorizedKey]): ResolvedAuthorizedKey =
      ResolvedAuthorizedKey(
        Vector(source),
        keys,
      )

    def apply(source: String, key: AuthorizedKey): ResolvedAuthorizedKey =
      ResolvedAuthorizedKey(
        Vector(source),
        Vector(key),
      )

  }

  case class ResolvedAuthorizedKey(
    source: Vector[String],
    keys: Vector[AuthorizedKey],
  ) {
    def withParent(parent: String) = copy(source = parent +: source)
    def lines =
      Vector(s"# ${source.mkString(" -> ")}") ++ keys.map(_.value)
  }

}
