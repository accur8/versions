package a8.versions.apps

import a8.appinstaller.AppInstallerConfig.LibDirKind
import a8.appinstaller.{AppInstaller, AppInstallerConfig, InstallBuilder}
import a8.shared.FileSystem
import a8.versions.Build.BuildType
import a8.versions._
import a8.versions.Upgrade.LatestArtifact
import a8.versions.apps.Main.{Conf, Runner}
import org.rogach.scallop.{ScallopConf, Subcommand}
import a8.versions.predef._
import coursier.core.{ModuleName, Organization}
import a8.shared.SharedImports._

import scala.annotation.tailrec

object Main {

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

    val resolve = new Subcommand("resolve") with Runner {

      val organization = opt[String](required = true, descr = "organization of the artifact to resolve")
      val artifact = opt[String](required = true, descr = "artifact name")
      val branch = opt[String](descr = "branch name")
      val version = opt[String](descr = "specific version")

      descr("setup app installer json files if they have not already been setup")

      override def run(main: Main) = {
        val r = this
        main.runResolve(coursier.Module(Organization(r.organization.apply()), ModuleName(r.artifact.apply())), r.branch.toOption, r.version.toOption)
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

      descr("install app into the installDir")

      override def run(main: Main) = {
        main
          .runInstall(
            coursier.Module(Organization(organization.apply()), ModuleName(artifact.apply())),
            branch.toOption,
            version.toOption,
            installDir.toOption.getOrElse("."),
            libDirKind.toOption,
            webappExplode.map(_.toBoolean).toOption
          )
      }

    }

    val buildDotSbt = new Subcommand("build_dot_sbt") with Runner {

      descr("generates the build.sbt and other sbt plumbing from the modules.conf file")

      override def run(main: Main) = {
        main.runGenerateBuildDotSbt()
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

    verify()

  }


  def main(args: Array[String]): Unit = {
    try {
      val main = new Main(args.toIndexedSeq)
      main.run()
      System.exit(0)
    } catch {
      case th: Throwable =>
        th.printStackTrace(System.err);
        System.exit(1)
    }
  }



}


class Main(args: Seq[String]) {


  implicit def buildType = BuildType.ArtifactoryBuild

  lazy val userHome = FileSystem.userHome
  lazy val a8Home = userHome \\ ".a8"
  lazy val a8VersionsCache = userHome \\ ".a8" \\ "versions" \\ "cache"

  lazy val conf = Conf(args)

  def run(): Unit = {
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


  def runResolve(module: coursier.Module, branch: Option[String], version: Option[String]): Unit = {

    val (resolvedVersion, latest) =
      (branch, version) match {
        case (None, None) =>
          sys.error("must supply a branch or version")
        case (Some(_), Some(_)) =>
          sys.error("must supply a branch or version not both")
        case (Some(b), None) =>
          LatestArtifact(module, b).resolveVersion(Map()) -> Some(s"latest_${b}.json")
        case (None, Some(v)) =>
          Version.parse(v).get -> None
      }

    println(s"using version ${resolvedVersion}")

    val tree = RepositoryOps.resolveDependencyTree(module, resolvedVersion)

    val aic =
      AppInstallerConfig(
        organization = module.organization.value,
        artifact = module.name.value,
        version = resolvedVersion.toString,
        branch = None,
      )

    val installBuilder = InstallBuilder(aic)

    val inventoryDir = a8VersionsCache \\ module.organization.value \\ module.name.value

    inventoryDir.makeDirectories()

    val inventoryFiles =
      Some(inventoryDir \ s"${resolvedVersion.toString}.json") ++ latest.map(inventoryDir \ _)

    val inventoryJson = installBuilder.inventory.prettyJson

    inventoryFiles.foreach(_.write(inventoryJson))

    println(s"resolved ${inventoryFiles}")

  }

  // same method as a8.sbt_a8.scrubBranchName() in sbt-a8 project
  def scrubBranchName(unscrubbedName: String): String = {
    unscrubbedName
      .filter(ch => ch.isLetterOrDigit)
      .toLowerCase
  }

  def runInstall(
    module: coursier.Module,
    branch: Option[String],
    version: Option[String],
    installDir: String,
    libDirKind: Option[String],
    webappExplode: Option[Boolean] = Some(true)
  ): Unit = {

    val (resolvedVersion, latest) =
      (branch, version) match {
        case (None, None) =>
          sys.error("must supply a branch or version")
        case (Some(_), Some(_)) =>
          sys.error("must supply a branch or version not both")
        case (Some(b), None) =>
          val resolvedBranch = scrubBranchName(b)
          LatestArtifact(module, resolvedBranch).resolveVersion(Map()) -> Some(s"latest_${resolvedBranch}.json")
        case (None, Some(v)) =>
          Version.parse(v).get -> None
      }

    val kind: Option[LibDirKind] =
      libDirKind
        .flatMap{ k =>
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
      )

    AppInstaller(config).execute()

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
    Build.upgrade(FileSystem.dir("."))
  }

}
