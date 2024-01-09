package io.accur8.neodeploy


import a8.shared.{CascadingHocon, CompanionGen, ConfigMojo, ConfigMojoOps, Exec, HoconOps, StringValue, ZRefreshable, ZString}
import io.accur8.neodeploy.VFileSystem.{Directory, File, dir}
import model.*
import SharedImports.*
import a8.shared.ZString.ZStringer
import a8.common.logging.LoggingF
import a8.shared.json.{JsonCodec, JsonReader}
import a8.shared.json.ast.{JsDoc, JsNothing, JsObj, JsVal}
import zio.{Cause, Chunk, UIO, ZIO}
import PredefAssist.*
import a8.shared.json.JsonReader.ReadResult
import com.typesafe.config.ConfigFactory
import io.accur8.neodeploy.Mxresolvedmodel.MxLoadedApplicationDescriptor
import io.accur8.neodeploy.plugin.{PgbackrestServerPlugin}
import io.accur8.neodeploy.resolvedmodel.ResolvedApp.{LoadedApplicationDescriptor, loadDescriptorFromDisk}
import io.accur8.neodeploy.resolvedmodel.ResolvedUser
import io.accur8.neodeploy.systemstate.SystemStateModel.{Command, Environ, M}
import zio.cache.Lookup

object resolvedmodel extends LoggingF {

  import a8.Scala3Hacks.*

  object ResolvedUser {

    val live: ZIO[UserLogin with ResolvedRepository with LocalDeploy.Config, Throwable, ResolvedUser] =
      for {
        rs <- ResolvedServer.live
        ul <- zservice[UserLogin]
        ru <- rs.fetchUserZ(ul)
      } yield ru

  }
  case class ResolvedUser(
    descriptor: UserDescriptor,
    home: Directory,
    server: ResolvedServer,
    loadedApplicationDescriptors: Vector[LoadedApplicationDescriptor],
  ) {

    def resolveLoginKeys: N[Vector[ResolvedAuthorizedKey]] =
      publicKey
        .flatMap {
          case Some(pk) =>
            zsucceed(Vector(ResolvedAuthorizedKey(qualifiedUserName.value, pk.asAuthorizedKey)))
          case None =>
            server.repository.publicKeys(qualifiedUserName)
        }

    lazy val gitAppsDirectory =
      server.gitServerDirectory.subdir(descriptor.login.value)

    lazy val resolvedApps: Vector[ResolvedApp] =
      loadedApplicationDescriptors
        .map(lad => ResolvedApp(lad, this))

    lazy val plugins = UserPlugin.UserPlugins(this)

    lazy val a8VersionsExec =
      descriptor
        .a8VersionsExec
        .orElse(server.descriptor.a8VersionsExec)
        .getOrElse("/usr/local/bin/a8-versions")
//        .getOrElse("a8-versions")

    lazy val appsRootDirectory: AppsRootDirectory =
      descriptor
        .appInstallDirectory
        .getOrElse(AppsRootDirectory(home.subdir("apps").path))

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

    def publicKey: N[Option[PublicKey]] =
      sshPublicKeyFileInRepo
        .readAsStringOpt
        .map(
          _.map(line => PublicKey(line))
        )

    def resolvedAuthorizedKeys(stack: Set[QualifiedUserName]): N[Vector[ResolvedAuthorizedKey]] = {
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

  object ResolvedServer {
    val live =
      for {
        config <- zservice[LocalDeploy.Config]
        resolvedRepository <- zservice[ResolvedRepository]
      } yield resolvedRepository.server(config.serverName)
  }

  case class ResolvedServer(
    descriptor: ServerDescriptor,
    gitServerDirectory: GitServerDirectory,
    repository: ResolvedRepository,
  ) {

    def applications =
      resolvedUsers
        .flatMap(_.resolvedApps)

    def virtualHosts = {
      val apps = resolvedUsers.flatMap(_.resolvedApps)
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

    def fetchUserZ(login: UserLogin): ZIO[Any, Throwable, ResolvedUser] =
      fetchUserOpt(login)
        .map(u => zsucceed(u))
        .getOrElse(zfail(new RuntimeException(z"user ${login} not found")))

    def fetchUserOpt(login: UserLogin): Option[ResolvedUser] =
      resolvedUsers
        .find(_.login == login)

    lazy val resolvedUsers: Vector[ResolvedUser] =
      descriptor
        .users
        .map( userDescriptor =>
          ResolvedUser(
            descriptor = userDescriptor,
            home = userDescriptor.resolvedHome,
            server = this,
            loadedApplicationDescriptors = repository.fetchLoadedApplicationDescriptors(this.name, userDescriptor.login),
          )
        )

    def supervisorCommand(action: String, applicationName: ApplicationName): Command =
      Command(
        descriptor.supervisorctlExec.getOrElse("supervisorctl"),
        action,
        applicationName.value
      )

    def execCommand(command: Command): N[Unit] = {
      val logLinesEffect: Chunk[String] => UIO[Unit] = { (lines: Chunk[String]) =>
        loggerF.debug(s"command output chunk -- ${lines.mkString("\n    ", "\n    ", "\n    ")}")
      }
      command
        .exec(logLinesEffect = logLinesEffect)
        .as(())
    }

    def vpnName = descriptor.vpnDomainName
    def name = descriptor.name

//    def supervisorDirectory: SupervisorDirectory = descriptor.supervisorDirectory
//    def caddyDirectory: CaddyDirectory = descriptor.caddyDirectory

  }

  object ResolvedApp {

    object LoadedApplicationDescriptor extends MxLoadedApplicationDescriptor
    @CompanionGen
    case class LoadedApplicationDescriptor(
      appConfigDir: Directory,
      serverName: ServerName,
      userLogin: UserLogin,
      descriptor: ApplicationDescriptor,
    )

    def loadDescriptorFromDisk(userLogin: UserLogin, serverName: ServerName, appConfigDir: Directory, appsRootDirectory: AppsRootDirectory): N[Option[LoadedApplicationDescriptor]] = {
      import a8.shared.ZFileSystem.SymlinkHandlerDefaults.follow
      val appDescriptorFilesZ =
        Vector(
          // this needs to be first so that this overrides the version set in the other files
          appConfigDir.file("version.properties"),
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
          .flatMap(_.map(_.zfile).sequence)

      val appDir = appsRootDirectory.subdir(appConfigDir.name)


      val baseConfigMap =
        Map(
          "appDir" -> appDir.path,
          "dataDir" -> appDir.subdir("data").path,
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
                configs.foldLeft(baseConfig) { case (acc, cfg) =>
                  cfg.resolveWith(acc)
                }

              val readResult = JsonReader[ApplicationDescriptor].readResult(resolvedConfig)

              def logWarnings =
                if (readResult.warnings.nonEmpty) {
                  loggerF.warn(s"found the following warnings reading the applicationDescriptor from ${appDescriptorFiles.mkString(" ")}\n${readResult.warnings.mkString("\n").indent("    ")}")
                } else {
                  zunit
                }

              val effect =
                readResult match {
                  case ReadResult.Success(descriptor, _, _, _) =>
                    zsucceed(LoadedApplicationDescriptor(appConfigDir, serverName, userLogin, descriptor).some)
                  case ReadResult.Error(re, _, _) =>
                    zfail(new RuntimeException(s"error reading application descriptor from ${appDescriptorFiles.mkString(" ")} -- ${re.prettyMessage}"))
                }

              logWarnings *> effect

            }
          } catch {
            case IsNonFatal(e) =>
              zfail(new RuntimeException(s"Failed to load application descriptor file: $appDescriptorFiles", e))
          }

        }
    }
  }

  case class ResolvedApp(
    loadedApplicationDescriptor: LoadedApplicationDescriptor,
    user: ResolvedUser,
  ) {
    if ( loadedApplicationDescriptor.appConfigDir.name.toLowerCase != loadedApplicationDescriptor.descriptor.name.value.toLowerCase ) {
      sys.error(z"mismatch between application folder name ${loadedApplicationDescriptor.appConfigDir.absPath} and configured application name ${loadedApplicationDescriptor.descriptor.name}")
    }
    def qname: String =
      loadedApplicationDescriptor
        .descriptor
        .resolvedDomainNames
        .headOption
        .map(_.value)
        .getOrElse(z"${user.qname}:${name}")
    def isNamed(appName: DomainName): Boolean =
      loadedApplicationDescriptor.descriptor.resolvedDomainNames.contains(appName)
    val descriptor: ApplicationDescriptor = loadedApplicationDescriptor.descriptor
    val gitDirectory: Directory = loadedApplicationDescriptor.appConfigDir
    val server = user.server
    val name = descriptor.name
    val appDirectory: Directory = user.appsRootDirectory.subdir(descriptor.name.value)
  }


  object ResolvedRepository {

    def loadFromDisk(gitRootDirectory: GitRootDirectory): N[ResolvedRepository] = {
      gitRootDirectory
        .unresolved
        .zdir
        .flatMap { dir =>
          zblock {
            val cascadingHocon =
              CascadingHocon
                .loadConfigsInDirectory(dir.asNioPath, recurse = false)
                .resolve
            val result =
              ConfigMojoOps.impl.ConfigMojoRoot(
                cascadingHocon.config.root(),
                cascadingHocon,
              )
            result
          }
        }
        .flatMap(_.asF[RepositoryDescriptor])
        .logError(s"error loading repository descriptor from ${gitRootDirectory}")
        .flatMap { repositoryDescriptor =>
          repositoryDescriptor
            .serversAndUsers
            .map { case (server, user) =>
              val userAppsDir = gitRootDirectory.subdir(server.name.value).subdir(user.login.value)
              val effect: N[Iterable[LoadedApplicationDescriptor]] =
                userAppsDir
                  .subdirs
                  .flatMap { appConfDirs =>
                    val effect: N[Iterable[LoadedApplicationDescriptor]] =
                      appConfDirs
                        .map { appConfDir =>
                          ResolvedApp.loadDescriptorFromDisk(user.login, server.name, appConfDir, user.resolvedAppsRootDirectory)
                            .trace(z"loadDescriptorFromDisk(${user.login}, ${server.name}, ${appConfDir.absPath})")
                        }
                        .toVector
                        .sequencePar
                        .map(_.flatten)
                    effect
                  }
              effect
            }
            .sequencePar
            .map(_.flatten)
            .map { loadedApplicationDescriptors =>
              ResolvedRepository(repositoryDescriptor, gitRootDirectory, loadedApplicationDescriptors)
            }
        }
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
    loadedApplicationDescriptors: Vector[LoadedApplicationDescriptor],
  ) {

    lazy val applicationDescriptorsByUserLogin: Map[(ServerName,UserLogin), Vector[LoadedApplicationDescriptor]] =
      loadedApplicationDescriptors
        .groupBy(lad => lad.serverName -> lad.userLogin)

    lazy val applicationByDomainName: Map[DomainName, ResolvedApp] =
      applications
        .flatMap(a => a.descriptor.resolvedDomainNames.map(_ -> a))
        .toMap

    def fetchLoadedApplicationDescriptors(serverName: ServerName, login: UserLogin): Vector[LoadedApplicationDescriptor] =
      applicationDescriptorsByUserLogin
        .getOrElse(serverName -> login, Vector.empty)


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

    def virtualHosts: Vector[VirtualHost] = servers.flatMap(_.virtualHosts)

    def applications: Vector[ResolvedApp] =
      users
        .flatMap(_.resolvedApps)

    def userOpt(userLogin: UserLogin, serverName: ServerName): Option[ResolvedUser] =
      serverOpt(serverName)
        .flatMap(_.fetchUserOpt(userLogin))

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

    def serverOpt(serverName: ServerName): Option[ResolvedServer] =
      servers
        .find(_.name == serverName)

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
    def resolvedAuthorizedKeys(id: QualifiedUserName, stack: Set[QualifiedUserName]): N[Vector[ResolvedAuthorizedKey]] = {

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

    lazy val servers: Vector[ResolvedServer] =
      descriptor
        .servers
        .map { serverDescriptor =>
          ResolvedServer(
            serverDescriptor,
            GitServerDirectory(gitRootDirectory.subdir(serverDescriptor.name.value).path),
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

    def resolveKeys(stack: Set[QualifiedUserName]): N[Vector[ResolvedAuthorizedKey]] = {

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

      val keysFromMembersZ: N[Vector[ResolvedAuthorizedKey]] =
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
