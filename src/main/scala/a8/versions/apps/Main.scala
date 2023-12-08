package a8.versions.apps


import a8.appinstaller.AppInstallerConfig.LibDirKind
import a8.appinstaller.{AppInstaller, AppInstallerConfig, InstallBuilder}
import a8.common.logging.LoggingBootstrapConfig
import a8.shared.{FileSystem, FromString, StringValue}
import a8.versions.Build.BuildType
import a8.versions.*
import a8.versions.Upgrade.LatestArtifact
import a8.versions.apps.Main.Runner
import a8.versions.predef.*
import io.accur8.neodeploy.SharedImports.{zblock, *}
import a8.shared.ZString.ZStringer
import a8.shared.app.BootstrappedIOApp.BootstrapEnv
import a8.shared.app.{BootstrappedIOApp, Bootstrapper}
import a8.versions.GenerateJavaLauncherDotNix.Parms
import a8.versions.PromoteArtifacts.Dependencies
import a8.versions.RepositoryOps.{RepoConfigPrefix, default}
import a8.versions.model.{BranchName, ResolutionRequest, ResolvedRepo}
import io.accur8.neodeploy.model.{ApplicationName, DomainName, ServerName, UserLogin}
import io.accur8.neodeploy.resolvedmodel.ResolvedRepository

import scala.annotation.tailrec
import io.accur8.neodeploy.{DeploySubCommand, ValidateRepo, Runner as NeodeployRunner}
import org.rogach.scallop.*
import zio.{ExitCode, Scope, ZIO, ZIOAppArgs, ZLayer}
import io.accur8.neodeploy.model.*
import a8.common.logging.Level
import ch.qos.logback.classic.LoggerContext

object Main extends BootstrappedIOApp {

  import a8.Scala3Hacks.*

  trait Runner {
    def runZ(main: Main): zio.ZIO[BootstrapEnv, Throwable, Unit]
  }

  case class ResolvedArgs(args: Seq[String]) {
    lazy val defaultLogLevel: Level = {
      if (args.contains("--debug")) {
        Level.Debug
      } else if (args.contains("--trace")) {
        Level.Trace
      } else {
        Level.Info
      }
    }
    lazy val consoleLogging: Boolean = defaultLogLevel != Level.Info
  }


  override def provideLayers(effect: ZIO[BootstrapEnv with LoggingBootstrapConfig & LoggerContext, Throwable, Unit]): ZIO[Any with ZIOAppArgs with Scope, Any, Any] =
    zservice[ZIOAppArgs].flatMap { args =>
      val resolvedArgs = ResolvedArgs(args.getArgs)
      val loggingBootstrapConfig =
        LoggingBootstrapConfig(
          overrideSystemErr = false,
          overrideSystemOut = false,
          setDefaultUncaughtExceptionHandler = true,
          fileLogging = false,
          consoleLogging = resolvedArgs.consoleLogging,
          hasColorConsole = LoggingBootstrapConfig.defaultHasColorConsole,
          appName = "a8-versions",
          defaultLogLevel = resolvedArgs.defaultLogLevel,
        )
      effect
        .provideSome[zio.Scope & zio.ZIOAppArgs](
            ZLayer.succeed(org.slf4j.LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]),
            Bootstrapper.layer,
            zl_succeed(loggingBootstrapConfig),
            layers.appName,
            layers.logsDir,
            layers.tempDir,
            layers.dataDir,
            layers.cacheDir,
            layers.workDir,
            layers.bootstrapConfig,
            loggingLayer,
            zio.logging.removeDefaultLoggers,
          )
    }


  override def runT: ZIO[BootstrapEnv, Throwable, Unit] = {
    zservice[ZIOAppArgs]
      .flatMap { args =>
        val main = new Main(args.getArgs.toSeq)
        main.runT
      }
      .either
      .flatMap {
        case Left(th) =>
          logger.fatal("fatal error on main fiber exiting with exitCode = 1", th)
          th.printStackTrace(System.err)
          exit(ExitCode.failure)
        case Right(_) =>
          logger.info("successful program completion exiting with exitCode = 0")
          exit(ExitCode.success)
      }
  }


  def runInstall(
    module: coursier.Module,
    branch: Option[BranchName],
    version: Option[Version],
    installDir: String,
    libDirKind: Option[String],
    webappExplode: Option[Boolean] = Some(true),
    backup: Boolean = true,
    repositoryOps: RepositoryOps,
  ): Unit = {

    implicit val buildType = BuildType.ArtifactoryBuild

    val (resolvedVersion: ParsedVersion, latest: Option[String]) =
      (branch, version) match {
        case (None, None) =>
          sys.error("must supply a branch or version")
        case (Some(_), Some(_)) =>
          sys.error("must supply a branch or version not both")
        case (Some(b), None) =>
          val resolvedBranch = scrubBranchName(b)
          LatestArtifact(module, resolvedBranch).resolveVersion(Map(), repositoryOps) -> Some(s"latest_${resolvedBranch}.json")
        case (None, Some(v)) =>
          ParsedVersion.parse(v.value).get -> None
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

    import io.accur8.neodeploy

    val config =
      AppInstallerConfig(
        organization = neodeploy.model.Organization(module.organization.value),
        artifact = neodeploy.model.Artifact(module.name.value),
        branch = None,
        version = resolvedVersion,
        installDir = Some(installDir),
        libDirKind = kind,
        webappExplode = webappExplode,
        backup = backup,
      )

    AppInstaller(config, repositoryOps).execute()

  }

  // same method as a8.sbt_a8.scrubBranchName() in sbt-a8 project
  def scrubBranchName(unscrubbedName: BranchName): BranchName = {
    BranchName(
      unscrubbedName
        .value
        .filter(ch => ch.isLetterOrDigit)
        .toLowerCase
    )
  }


//  given ValueConverter[Organization] = stringValueValueConverter[Organization]
//
//  given ValueConverter[Artifact] = stringValueValueConverter[Artifact]
//
//  given ValueConverter[BranchName] = stringValueValueConverter[BranchName]
//
//  given ValueConverter[Version] = stringValueValueConverter[Version]

  implicit def stringValueValueConverter[A <: StringValue : FromString]: ValueConverter[A] =
    new ValueConverter[A]:

      lazy val fromString = implicitly[FromString[A]]

      override def parse(s: List[(String, List[String])]): Either[String, Option[A]] =
        Right(
          s.flatMap(t =>
            t._2.flatMap(v =>
              fromString.fromString(v)
            )
          ).headOption
        )

      override val argType: ArgType.V = ArgType.SINGLE

}


case class Main(args: Seq[String]) {

  implicit def buildType: BuildType = BuildType.ArtifactoryBuild

  lazy val userHome = FileSystem.userHome
  lazy val a8Home = userHome \\ ".a8"
  lazy val a8VersionsCache = userHome \\ ".a8" \\ "versions" \\ "cache"

  lazy val conf = Conf(args)

  def runT: zio.ZIO[BootstrapEnv, Throwable, Unit] = zsuspend {
    conf.impl.setupVerbosity()
    val subcommand = conf.subcommand
    subcommand match {
      case Some(r: Runner) =>
        r.runZ(this)
      case _ =>
        if (args.nonEmpty) {
          zfail(new RuntimeException(s"don't know how to handle -- ${args}"))
        } else {
          zblock(conf.printHelp())
        }
    }
  }


  def runResolve(module: coursier.Module, branch: Option[BranchName], version: Option[Version], repositoryOps: RepositoryOps): Unit = {

    val (resolvedVersion: ParsedVersion, latest: Option[String]) =
      (branch, version) match {
        case (None, None) =>
          sys.error("must supply a branch or version")
        case (Some(_), Some(_)) =>
          sys.error("must supply a branch or version not both")
        case (Some(b), None) =>
          LatestArtifact(module, b).resolveVersion(Map(), repositoryOps) -> Some(s"latest_${b}.json")
        case (None, Some(v)) =>
          ParsedVersion.parse(v.value).get -> None
      }

    println(s"using version ${resolvedVersion}")

    val tree = repositoryOps.resolveDependencyTree(module, resolvedVersion)

    import io.accur8.neodeploy

    val aic =
      AppInstallerConfig(
        organization = neodeploy.model.Organization(module.organization.value),
        artifact = neodeploy.model.Artifact(module.name.value),
        version = resolvedVersion,
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
