package a8.versions.apps

import a8.appinstaller.AppInstallerConfig.LibDirKind
import a8.appinstaller.{AppInstaller, AppInstallerConfig, InstallBuilder}
import a8.shared.{FileSystem, FromString}
import a8.versions.Build.BuildType
import a8.versions._
import a8.versions.Upgrade.LatestArtifact
import a8.versions.apps.Main.{Conf, Runner}
import org.rogach.scallop.{ScallopConf, ScallopOption, Subcommand}
import a8.versions.predef._
import coursier.core.{ModuleName, Organization}
import a8.shared.SharedImports._
import a8.shared.ZString.ZStringer
import a8.shared.app.A8LogFormatter
import a8.versions.RepositoryOps.RepoConfigPrefix
import a8.versions.model.BranchName
import io.accur8.neodeploy.PushRemoteSyncSubCommand.Filter
import io.accur8.neodeploy.Sync.SyncName
import io.accur8.neodeploy.model.{ApplicationName, ServerName, UserLogin}
import wvlet.log.{LogLevel, Logger}

import scala.annotation.tailrec
import io.accur8.neodeploy.{InfrastructureSetupSubCommand, PushRemoteSyncSubCommand, ValidateRepo, Runner => NeodeployRunner}

object Main extends Logging {

  sealed trait Runner {
    def run(main: Main): Unit
  }

  case class Conf(args0: Seq[String]) extends ScallopConf(args0) {

    banner(
      s"""
         |Accur8 Version Tools
         |
         |Example:
         |  a8-versions resolve --organization a8 --artifact a8-zoolander_2.12 --branch master
         |
         |Usage: a8-versions [Subcommand] [arg[...]]
         |
         |  * If you want to see the options for the app launcher (like how to update the app) then use --l-help arg:
         |      a8-zoo --l-help
         |""".stripMargin
    )

    val debug = opt[Boolean](descr = "show debug level logging, all logging except trace")
    val trace = opt[Boolean](descr = "show trace level logging, this has the most detail and include debug logging")

    lazy val defaultLogLevel =
      (debug.toOption, trace.toOption) match {
        case (_, Some(true)) =>
          LogLevel.TRACE
        case (Some(true), _) =>
          LogLevel.DEBUG
        case _ =>
          LogLevel.INFO
      }

    def setupVerbosity(): Unit = {

      val categories =
        Seq(
          "io.accur8.neodeploy",
          "a8.versions",
        )

      categories
        .foreach(c =>
          wvlet.log.Logger(c).setLogLevel(defaultLogLevel)
        )


      System.setProperty("defaultLogLevel", defaultLogLevel.name)

    }

    def repositoryOps(repo: ScallopOption[String]): RepositoryOps =
      repo
        .map(v => RepositoryOps.apply(RepoConfigPrefix(v)))
        .getOrElse(RepositoryOps.default)

    val resolve = new Subcommand("resolve") with Runner {

      val organization = opt[String](required = true, descr = "organization of the artifact to resolve")
      val artifact = opt[String](required = true, descr = "artifact name")
      val branch = opt[String](descr = "branch name")
      val version = opt[String](descr = "specific version")

      val repo: ScallopOption[String] = opt[String](descr = "repository name", required = false)

      descr("setup app installer json files if they have not already been setup")

      override def run(main: Main) = {
        val r = this
        val ro = repositoryOps(r.repo)
        main.runResolve(coursier.Module(Organization(r.organization.apply()), ModuleName(r.artifact.apply())), r.branch.toOption, r.version.toOption, ro)
      }

    }

    val install = new Subcommand("install") with Runner {

      val organization = opt[String](descr = "organization of the artifact to resolve", required = true)
      val artifact = opt[String](descr = "artifact name", required = true)
      val branch = opt[String](descr = "branch name", required = false)
      val version = opt[String](descr = "specific version", required = false)
      val installDir = opt[String](descr = "the install directory", required = true)
      val libDirKind = opt[String](descr = "lib directory kind", required = false)
      val webappExplode = opt[String](descr = "do webapp explode", required = false)
      val backup = opt[String](descr = "run backup of existing install before install", required = false)

      val repo = opt[String](descr = "repository name", required = false)

      descr("install app into the installDir")

      override def run(main: Main) = {
        Main
          .runInstall(
            coursier.Module(Organization(organization.apply()), ModuleName(artifact.apply())),
            branch.toOption,
            version.toOption,
            installDir.toOption.getOrElse("."),
            libDirKind.toOption,
            webappExplode.map(_.toBoolean).toOption,
            backup = backup.toOption.map(_.toBoolean).getOrElse(true),
            repositoryOps = repositoryOps(repo),
          )
      }

    }

    val buildDotSbt = new Subcommand("build_dot_sbt") with Runner {

      descr("generates the build.sbt and other sbt plumbing from the modules.conf file")

      override def run(main: Main) = {
        main.runGenerateBuildDotSbt()
      }

    }

    val syncsDescription = "comma separated list of syncs to run [ authorized_keys2 | caddy | supervisor | installer | pgbackrestClient | pgbackrestServer | rsnapshotClient | rsnapshotServer ]"

    val pushRemoteSync = new Subcommand("push_remote_sync") with Runner {

      descr("pushes a sync to a remote server")

      val server = opt[String](descr = "server to push to", required = false)
      val servers = opt[String](descr = "comma separated list of servers to push to", required = false)

      val user = opt[String](descr = "user to push to", required = false)
      val users = opt[String](descr = "comma separated list of users to push to", required = false)

      val app = opt[String](descr = "app to push", required = false)
      val apps = opt[String](descr = "comma separated list of apps to push", required = false)

      val sync = opt[String](descr = "sync to run", required = false)
      val syncs = opt[String](descr = syncsDescription, required = false)

      override def run(main: Main) = {
        NeodeployRunner(
          serversFilter = resolveArgs[ServerName](server, servers),
          usersFilter = resolveArgs[UserLogin](user, users),
          appsFilter = resolveArgs[ApplicationName](app, apps),
          syncsFilter = resolveArgs[SyncName](sync, syncs),
          debug.toOption.getOrElse(false),
          trace.toOption.getOrElse(false),
          (rr, parms) => PushRemoteSyncSubCommand(rr, parms).run,
          defaultLogLevel = defaultLogLevel,
        ).unsafeRun()
      }

    }

    val validateServerAppConfigs = new Subcommand("validate_server_app_configs") with Runner {

      descr("will validate the server app config repo, for example creating any missing ssh keys")

      override def run(main: Main) =
        NeodeployRunner(runnerFn = (rr, parms) => ValidateRepo(rr).run, defaultLogLevel = defaultLogLevel)
          .unsafeRun()

    }

    val dnsSetup = new Subcommand("dns_setup") with Runner {

      descr("will setup the various dns servers")

      override def run(main: Main) =
        NeodeployRunner(runnerFn = (rr, parms) => InfrastructureSetupSubCommand(rr).run, defaultLogLevel = defaultLogLevel)
          .unsafeRun()

    }

    def resolveArgs[A: FromString: ZStringer](singleArg: ScallopOption[String], csvArg: ScallopOption[String]): Filter[A] = {
      val values: Iterable[String] = singleArg.toOption ++ csvArg.toOption.toVector.flatMap(_.split(","))
      val fromString = implicitly[FromString[A]].fromString _
      Filter(
        csvArg.name,
        values
          .flatMap(fromString)
          .toVector
      )
    }

    val localUserSync = new Subcommand("local_user_sync") with Runner {

      descr("synchronizes the user settings and any apps that run under this user")

      val app = opt[String](descr = "sync this app only", required = false)
      val apps = opt[String](descr = "sync the comma separated list of apps", required = false)

      val sync = opt[String](descr = "sync to run", required = false)
      val syncs = opt[String](descr = syncsDescription, required = false)

      override def run(main: Main) = {
        val runLocalServer =
          io.accur8.neodeploy.LocalUserSyncSubCommand(
            resolveArgs[ApplicationName](app, apps),
            resolveArgs[SyncName](sync, syncs),
            defaultLogLevel,
          )
        runLocalServer.main(Array.empty)
      }

    }

    val gitignore = new Subcommand("gitignore") with Runner {

      descr("makes sure that .gitignore has the standard elements")

      override def run(main: Main) = {
        main.runGitignore()
      }

    }

    val version_bump = new Subcommand("version_bump") with Runner {

      descr("upgrades the versions in version.properties aka runs a version bump")

      override def run(main: Main) = {
        main.runVersionBump()
      }

    }

    addSubcommand(resolve)
    addSubcommand(install)
    addSubcommand(buildDotSbt)
    addSubcommand(gitignore)
    addSubcommand(version_bump)
    addSubcommand(pushRemoteSync)
    addSubcommand(localUserSync)
    addSubcommand(validateServerAppConfigs)
    addSubcommand(dnsSetup)

    verify()

  }

  lazy val logLevels =
    Seq(
      "jdk.event",
      "sun.net",
    )

  def main(args: Array[String]): Unit = {
    try {
      wvlet.airframe.log.init
      Logger.setDefaultFormatter(A8LogFormatter.ColorConsole)
      Logger.setDefaultLogLevel(LogLevel.DEBUG)
      logLevels.foreach(l => Logger(l).setLogLevel(LogLevel.INFO))
      val main = new Main(args.toIndexedSeq)
      main.run()
      System.exit(0)
    } catch {
      case th: Throwable =>
        th.printStackTrace(System.err);
        System.exit(1)
    }
  }


  def runInstall(
    module: coursier.Module,
    branch: Option[String],
    version: Option[String],
    installDir: String,
    libDirKind: Option[String],
    webappExplode: Option[Boolean] = Some(true),
    backup: Boolean = true,
    repositoryOps: RepositoryOps,
  ): Unit = {

    implicit val buildType = BuildType.ArtifactoryBuild

    val (resolvedVersion, latest) =
      (branch, version) match {
        case (None, None) =>
          sys.error("must supply a branch or version")
        case (Some(_), Some(_)) =>
          sys.error("must supply a branch or version not both")
        case (Some(b), None) =>
          val resolvedBranch = scrubBranchName(b)
          LatestArtifact(module, resolvedBranch).resolveVersion(Map(), repositoryOps) -> Some(s"latest_${resolvedBranch}.json")
        case (None, Some(v)) =>
          Version.parse(v).get -> None
      }

    val kind: Option[LibDirKind] =
      libDirKind
        .flatMap { k =>
          val result: Option[LibDirKind] = LibDirKind
            .values
            .find(_.entryName.equalsIgnoreCase(k))
          if (result.isEmpty) {
            sys.error(s"libDirKind entered does not match case insensitive value in ${LibDirKind.values.map(_.entryName).mkString("['", "', '", "']")}")
          }
          result
        }
        .orElse(Some(LibDirKind.Symlink))

    val config =
      AppInstallerConfig(
        organization = module.organization.value,
        artifact = module.name.value,
        branch = None,
        version = resolvedVersion.toString,
        installDir = Some(installDir),
        libDirKind = kind,
        webappExplode = webappExplode,
        backup = backup,
      )

    AppInstaller(config, repositoryOps).execute()

  }

  // same method as a8.sbt_a8.scrubBranchName() in sbt-a8 project
  def scrubBranchName(unscrubbedName: String): BranchName = {
    BranchName(
      unscrubbedName
        .filter(ch => ch.isLetterOrDigit)
        .toLowerCase
    )
  }
}


case class Main(args: Seq[String]) {

  implicit def buildType = BuildType.ArtifactoryBuild

  lazy val userHome = FileSystem.userHome
  lazy val a8Home = userHome \\ ".a8"
  lazy val a8VersionsCache = userHome \\ ".a8" \\ "versions" \\ "cache"

  lazy val conf = Conf(args)

  def run(): Unit = {
    conf.setupVerbosity()
    conf.subcommand match {
      case Some(r: Runner) =>
        r.run(this)
      case _ =>
        if (args.nonEmpty) {
          sys.error(s"don't know how to handle -- ${args}")
        }
        conf.printHelp()
    }
  }


  def runResolve(module: coursier.Module, branch: Option[String], version: Option[String], repositoryOps: RepositoryOps): Unit = {

    val (resolvedVersion, latest) =
      (branch, version) match {
        case (None, None) =>
          sys.error("must supply a branch or version")
        case (Some(_), Some(_)) =>
          sys.error("must supply a branch or version not both")
        case (Some(b), None) =>
          LatestArtifact(module, BranchName(b.trim)).resolveVersion(Map(), repositoryOps) -> Some(s"latest_${b}.json")
        case (None, Some(v)) =>
          Version.parse(v).get -> None
      }

    println(s"using version ${resolvedVersion}")

    val tree = repositoryOps.resolveDependencyTree(module, resolvedVersion)

    val aic =
      AppInstallerConfig(
        organization = module.organization.value,
        artifact = module.name.value,
        version = resolvedVersion.toString,
        branch = None,
      )

    val installBuilder = InstallBuilder(aic, repositoryOps)

    val inventoryDir = a8VersionsCache \\ module.organization.value \\ module.name.value

    inventoryDir.makeDirectories()

    val inventoryFiles =
      Some(inventoryDir \ s"${resolvedVersion.toString}.json") ++ latest.map(inventoryDir \ _)

    val inventoryJson = installBuilder.inventory.prettyJson

    inventoryFiles.foreach(_.write(inventoryJson))

    println(s"resolved ${inventoryFiles}")

  }

  def runGenerateBuildDotSbt(): Unit = {

    val d = FileSystem.dir(".")

    val buildDotSbtGenerator = new BuildDotSbtGenerator(d)
    buildDotSbtGenerator.run()

    if ( buildDotSbtGenerator.firstRepo.astRepo.gradle ) {
      val g = new GradleGenerator(d)
      g.run()
    }

  }

  def runGitignore(): Unit = {
    UpdateGitIgnore.update(new java.io.File(".gitignore"))
  }

  def runVersionBump(): Unit = {
    Build.upgrade(FileSystem.dir("."), RepositoryOps.default)
  }

}
