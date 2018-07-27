package a8.versions.apps

import a8.appinstaller.AppInstallerConfig.LibDirKind
import a8.appinstaller.{AppInstaller, AppInstallerConfig, InstallBuilder}
import a8.versions.Build.BuildType
import a8.versions._
import a8.versions.Upgrade.LatestArtifact
import a8.versions.apps.Main.{Conf, Runner}
import org.rogach.scallop.{ScallopConf, Subcommand}
import a8.versions.predef._
import m3.fs.Directory

object Main {

  sealed trait Runner {
    def run(main: Main): Unit
  }

  case class Conf(args0: Seq[String]) extends ScallopConf(args0) {

    banner(
      s"""
         |Accur8 Version Tools
         |
         |Example: a8-versions resolve --organization a8 --artifact a8-zoolander_2.12 --branch master
         |
         |Usage: a8-versions [Subcommand] [arg[...]]
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
        main.runResolve(coursier.Module(r.organization.apply(), r.artifact.apply()), r.branch.toOption, r.version.toOption)
      }

    }

    val install = new Subcommand("install") with Runner {

      val organization = opt[String](required = true, descr = "organization of the artifact to resolve")
      val artifact = opt[String](required = true, descr = "artifact name")
      val branch = opt[String](required = true, descr = "branch name")
      val version = opt[String](descr = "specific version")
      var installDir = opt[String](descr = "the install directory", required = true)

      descr("install app into the installDir")

      override def run(main: Main) = {
        main.runInstall(coursier.Module(organization.apply(), artifact.apply()), branch.toOption, version.toOption, installDir.toOption.getOrElse("."))
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
      val main = new Main(args)
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

  lazy val userHome = m3.fs.dir(System.getProperty("user.home"))
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
        organization = module.organization,
        artifact = module.name,
        version = resolvedVersion.toString,
        branch = None,
      )

    val installBuilder = InstallBuilder(aic)

    val inventoryDir = a8VersionsCache \\ module.organization \\ module.name

    inventoryDir.makeDirectories()

    val inventoryFiles =
      Some(inventoryDir \ s"${resolvedVersion.toString}.json") ++ latest.map(inventoryDir \ _)

    val inventoryJson = toJsonPrettyStr(installBuilder.inventory)

    inventoryFiles.foreach(_.write(inventoryJson))

    println(s"resolved ${inventoryFiles}")

  }



  def runInstall(module: coursier.Module, branch: Option[String], version: Option[String], installDir: String): Unit = {

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

    val config =
      AppInstallerConfig(
        organization = module.organization,
        artifact = module.name,
        branch = None,
        version = resolvedVersion.toString,
        installDir = Some(installDir),
        libDirKind = Some(LibDirKind.Repo),
        webappExplode = Some(true),
      )

    AppInstaller(config).execute()

  }

  def runGenerateBuildDotSbt(): Unit = {
    val d = m3.fs.dir(".")
    val g = new BuildDotSbtGenerator(d)
    g.run()
  }

  def runGitignore(): Unit = {
    UpdateGitIgnore.update(new java.io.File(".gitignore"))
  }

  def runVersionBump(): Unit = {
    Build.upgrade(m3.fs.dir("."))
  }

}