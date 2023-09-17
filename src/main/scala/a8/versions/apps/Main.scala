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
import a8.shared.SharedImports.*
import a8.shared.ZString.ZStringer
import a8.shared.app.BootstrappedIOApp.BootstrapEnv
import a8.shared.app.BootstrappedIOApp
import a8.versions.GenerateJavaLauncherDotNix.Parms
import a8.versions.PromoteArtifacts.Dependencies
import a8.versions.RepositoryOps.RepoConfigPrefix
import a8.versions.model.{BranchName, ResolutionRequest, ResolvedRepo}
import io.accur8.neodeploy.model.{ApplicationName, DomainName, ServerName, UserLogin}
import io.accur8.neodeploy.resolvedmodel.ResolvedRepository

import scala.annotation.tailrec
import io.accur8.neodeploy.{DeploySubCommand, ValidateRepo, Runner as NeodeployRunner}
import org.rogach.scallop.*
import zio.{Scope, ZIO, ZIOAppArgs}
import io.accur8.neodeploy.model.*
import a8.common.logging.Level

object Main extends Logging {

  import a8.Scala3Hacks.*

  trait Runner {
    def run(main: Main): Unit
  }

  lazy val logLevels =
    Seq(
      "jdk.event",
      "sun.net",
    )

  case class ResolvedArgs(args: Seq[String]) {
    lazy val defaultLogLevel: Level = Level.Trace // ???
    lazy val consoleLogging: Boolean = true // ???
  }


  def main(args: Array[String]): Unit = {
    try {
      val resolvedArgs = ResolvedArgs(args)
      LoggingBootstrapConfig
        .finalizeConfig(
          LoggingBootstrapConfig(
            overrideSystemErr = true,
            overrideSystemOut = true,
            setDefaultUncaughtExceptionHandler = true,
            fileLogging = false,
            consoleLogging = resolvedArgs.consoleLogging,
            hasColorConsole = LoggingBootstrapConfig.defaultHasColorConsole,
            appName = "a8-versions",
            defaultLogLevel = resolvedArgs.defaultLogLevel,
          )
        )
      logLevels.foreach(l => a8.common.logging.Logger.logger(l).setLevel(Level.Info))
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

  def run(): Unit = {
    conf.impl.setupVerbosity()
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
