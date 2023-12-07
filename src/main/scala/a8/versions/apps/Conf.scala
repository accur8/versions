package a8.versions.apps


import org.rogach.scallop.ScallopConf
import org.rogach.scallop.*
import a8.versions.{GenerateJavaLauncherDotNix, PromoteArtifacts, RepositoryOps}
import RepositoryOps.RepoConfigPrefix
import a8.versions.PromoteArtifacts.Dependencies
import a8.versions.apps.Main.Runner
import a8.versions.model.*
import io.accur8.neodeploy.model.*
import a8.shared.SharedImports.*
import a8.shared.app.BootstrappedIOApp
import io.accur8.neodeploy.{DeployArg, DeploySubCommand, Layers, RawDeployArgs, SetupDatabase, ValidateRepo, resolvedmodel, Runner as NeodeployRunner}
import zio.ZIO
import a8.shared.ZString.ZStringer
import a8.shared.{FileSystem, FromString, ZFileSystem}
import a8.versions.GenerateJavaLauncherDotNix.Parms
import a8.shared.app.BootstrappedIOApp.BootstrapEnv
import a8.common.logging.Level
import a8.common.logging.Logger
import io.accur8.neodeploy.LocalDeploy.Config
import io.accur8.neodeploy.SharedImports.VFileSystem
import io.accur8.neodeploy.systemstate.SystemStateModel.PathLocator
import org.rogach.scallop.exceptions.ScallopResult

object Conf {

}

case class Conf(args0: Seq[String]) extends ScallopConf(args0) {

  import impl._

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

  val debug = opt[Boolean](name = "debug", descr = "show debug level logging, all logging except trace")
  val trace = opt[Boolean](name = "trace", descr = "show trace level logging, this has the most detail and include debug logging")

  lazy val defaultLogLevel =
    (debug.toOption, trace.toOption) match {
      case (_, Some(true)) =>
        Level.Trace
      case (Some(true), _) =>
        Level.Debug
      case _ =>
        Level.Info
    }

  object impl {

    val syncsDescription = "comma separated list of syncs to run [ authorized_keys2 | caddy | supervisor | installer | pgbackrestClient | pgbackrestServer | rsnapshotClient | rsnapshotServer ]"

    def setupVerbosity(): Unit = {

      val categories =
        Seq(
          "io.accur8.neodeploy",
          "a8.versions",
        )

      categories
        .foreach(c =>
          Logger.logger(c).setLevel(defaultLogLevel)
        )


      System.setProperty("defaultLogLevel", defaultLogLevel.name): @scala.annotation.nowarn

    }

    def repositoryOps(repo: ScallopOption[String]): RepositoryOps =
      repo
        .map(v => RepositoryOps.apply(RepoConfigPrefix(v)))
        .getOrElse(RepositoryOps.default)

    implicit def valueConverter[A: FromString]: ValueConverter[A] =
      org.rogach.scallop.singleArgConverter[A](
        s => FromString[A].fromString(s).get,
        PartialFunction.empty
      )


  }


  val resolve = new Subcommand("resolve") with Runner {

    val organization = opt[Organization](required = true, descr = "organization of the artifact to resolve")
    val artifact = opt[Artifact](required = true, descr = "artifact name")
    val branch = opt[BranchName](descr = "branch name")
    val version = opt[Version](descr = "specific version")

    val repo: ScallopOption[String] = opt[String](descr = "repository name", required = false)

    descr("setup app installer json files if they have not already been setup")

    //    import coursier._

    override def runZ(main: Main) = zblock {
      val r = this
      val ro = repositoryOps(r.repo)
      main.runResolve(coursier.Module(r.organization.apply().asCoursierOrg, r.artifact.apply().asCoursierModuleName), r.branch.toOption, r.version.toOption, ro)
    }

    def coursierModule(organization: ScallopOption[Organization], artifact: ScallopOption[Artifact]): coursier.Module =
      coursier.Module(organization().asCoursierOrg, artifact().asCoursierModuleName)

  }

  val promote = new Subcommand("promote") with Runner {

    val organization = opt[Organization](descr = "organization of the artifact to resolve")
    val artifact = opt[Artifact](required = true, descr = "artifact name")
    val version = opt[Version](required = true, descr = "specific version")
    val dependencies = opt[String](descr = "how to handle dependencies in the same organization [nothing|validate|promote]")

    descr("promote an artifact from accur8's private locus repo into the public maven repo")

    override def runZ(main: Main) = zblock {

      val resolvedDependency =
        dependencies
          .toOption
          .map(d => Dependencies.values.find(_.name.toLowerCase == d.toLowerCase).getOrError(s"invalid depdendency ${d}"))
          .getOrElse(Dependencies.Nothing)

      import io.accur8.neodeploy.model._

      val resolutionRequest =
        ResolutionRequest(
          organization = organization.toOption.getOrElse(PromoteArtifacts.IoDotAccur8Organization),
          artifact = artifact(),
          version = version(),
        )

      val promoteArtifacts =
        PromoteArtifacts(
          resolutionRequest,
          resolvedDependency,
        )

      object PromoteArtifactsMain extends BootstrappedIOApp {
        override def runT: ZIO[BootstrappedIOApp.BootstrapEnv, Throwable, Unit] =
          promoteArtifacts.runT
      }

      import coursier._
      PromoteArtifactsMain.main(Array())
    }

  }

  val install = new Subcommand("install") with Runner {

    val organization = opt[Organization](descr = "organization of the artifact to resolve", required = true)
    val artifact = opt[Artifact](descr = "artifact name", required = true)
    val branch = opt[BranchName](descr = "branch name", required = false)
    val version = opt[Version](descr = "specific version", required = false)
    val installDir = opt[String](descr = "the install directory", required = true)
    val libDirKind = opt[String](descr = "lib directory kind", required = false)
    val webappExplode = opt[Boolean](descr = "do webapp explode", required = false)
    val backup = opt[Boolean](descr = "run backup of existing install before install", required = false)

    val repo = opt[String](descr = "repository name", required = false)

    descr("install app into the installDir")

    override def runZ(main: Main) = zblock {
      Main
        .runInstall(
          coursier.Module(organization().asCoursierOrg, artifact().asCoursierModuleName),
          branch.toOption,
          version.toOption,
          installDir.toOption.getOrElse("."),
          libDirKind.toOption,
          webappExplode.toOption,
          backup = backup.toOption.getOrElse(true),
          repositoryOps = repositoryOps(repo),
        )
    }

  }

  val javaLauncherDotNix = new Subcommand("java_launcher_dot_nix") with Runner {

    val launcherJsonFile = opt[String](descr = "launcher json file", required = true)

    descr("generate java-launcher.nix file")

    override def runZ(main: Main) = {
      val parms: Parms =
        json.unsafeRead[Parms](
          FileSystem.file(launcherJsonFile())
            .readAsString()
        )

      val rawEffect =
        GenerateJavaLauncherDotNix(parms, Some(VFileSystem.userHome.subdir(".a8/versions/nixhashcache")))
          .buildDescriptionT
          .flatMap { buildDescription =>
            loggerF
              .info(
                s"""
  resolvedVersion ${buildDescription.resolvedVersion}
  ${buildDescription.files.map(f => s"================== ${f.filename}\n${f.contents}").mkString("\n")}
                  """
              )
          }

      Layers.provideN(rawEffect)

    }

  }

  val buildDotSbt = new Subcommand("build_dot_sbt") with Runner {

    descr("generates the build.sbt and other sbt plumbing from the modules.conf file")

    override def runZ(main: Main) = zblock {
      main.runGenerateBuildDotSbt()
    }

  }

  val deploy = new Subcommand("deploy") with Runner {

    descr("deploy an app to it's remote system")

    //    val app: ScallopOption[AppArg] = opt[AppArg](descr = "fully qualified app name", argName = "app[:version]", required = false)
    val appArgs: ScallopOption[RawDeployArgs] = trailArg[RawDeployArgs](descr = "fully qualified app names", required = true)

    override def runZ(main: Main) = {
      NeodeployRunner(
        remoteDebug = debug.toOption.getOrElse(false),
        remoteTrace = trace.toOption.getOrElse(false),
        runnerFn = { (resolvedRepo: resolvedmodel.ResolvedRepository, runner: io.accur8.neodeploy.Runner) =>
          val effect: zio.Task[Unit] =
            appArgs.toOption.get.resolve(resolvedRepo) match {
              case Right(deployArgs) =>
                val effect =
                  DeploySubCommand(resolvedRepo, runner, deployArgs)
                    .run
                Layers.provideN(effect, LocalRootDirectory.default)
              case Left(errorMsg) =>
                ZIO.fail(new RuntimeException(errorMsg))
            }
          effect
        },
      ).runT

    }

  }

  val setupDatabase = new Subcommand("setup-database") with Runner {

    descr("setup database(s) for the supplied apps (does not do zoo files)")

    //    val app: ScallopOption[AppArg] = opt[AppArg](descr = "fully qualified app name", argName = "app[:version]", required = false)
    val appArgs: ScallopOption[RawDeployArgs] = trailArg[RawDeployArgs](descr = "fully qualified app names", required = true)

    override def runZ(main: Main) = {
      loadResolvedRepository()
        .flatMap { resolvedRepo =>
          appArgs.toOption.get.resolve(resolvedRepo) match {
            case Left(error) =>
              ZIO.fail(new RuntimeException(error))
            case Right(deployArgs) =>
              val rawEffect = SetupDatabase.setupDatabases(resolvedRepo, deployArgs).scoped
              val effect = Layers.provideN(rawEffect, LocalRootDirectory.default)
              effect
          }
        }
    }
  }

  val validateServerAppConfigs = new Subcommand("validate_server_app_configs") with Runner {

    descr("will validate the server app config repo, for example creating any missing ssh keys")

    override def runZ(main: Main) =
      NeodeployRunner(runnerFn = (rr, parms) => ValidateRepo(rr).run)
        .runT

  }

  val localDeploy = new Subcommand("local_deploy") with Runner {

    descr("deploy an app on the local system")

    val appArgs: ScallopOption[RawDeployArgs] = trailArg[RawDeployArgs](descr = "fully qualified app names", required = true)

    override def runZ(main: Main) = {

      NeodeployRunner(
        remoteDebug = debug.toOption.getOrElse(false),
        remoteTrace = trace.toOption.getOrElse(false),
        runnerFn = { (resolvedRepo: resolvedmodel.ResolvedRepository, runner: io.accur8.neodeploy.Runner) =>
          appArgs.toOption.get.resolve(resolvedRepo) match {
            case Right(deployArgs) =>
              val runLocalDeploy =
                io.accur8.neodeploy.LocalDeploySubCommand(
                  deployArgs,
                )
              Layers.provide(
                runLocalDeploy.runM
              )
            case Left(errorMsg) =>
              ZIO.fail(new RuntimeException(errorMsg))
          }
        },
      ).runT

    }

  }

  val localDeployDev = new Subcommand("local_deploy_dev") with Runner {

    descr("developer / test harness for local_deploy an app on the local system")

    val user: ScallopOption[String] = opt(name = "user", descr = "user override", required = false)
    val rootDirectory: ScallopOption[String] = opt(name = "root", descr = "root directory override", required = false)
    val server: ScallopOption[String] = opt(name = "server", descr = "server override", required = false)
    val gitRootDirectory: ScallopOption[String] = opt(name = "gitRoot", descr = "git root directory override", required = false)

    val appArgs: ScallopOption[RawDeployArgs] = trailArg[RawDeployArgs](descr = "fully qualified app names", required = true)

    override def runZ(main: Main) = {

      val defaultConfig = Config.default()

      val config =
        Config(
          userLogin = user.toOption.map(UserLogin(_)).getOrElse(defaultConfig.userLogin),
          rootDirectory = rootDirectory.toOption.map(LocalRootDirectory(_)).getOrElse(defaultConfig.rootDirectory),
          serverName = server.toOption.map(ServerName(_)).getOrElse(defaultConfig.serverName),
          gitRootDirectory = gitRootDirectory.toOption.map(GitRootDirectory(_)).getOrElse(defaultConfig.gitRootDirectory),
        )

      NeodeployRunner(
        config = config.some,
        remoteDebug = debug.toOption.getOrElse(false),
        remoteTrace = trace.toOption.getOrElse(false),
        runnerFn = { (resolvedRepo: resolvedmodel.ResolvedRepository, runner: io.accur8.neodeploy.Runner) =>
          appArgs.toOption.get.resolve(resolvedRepo) match {
            case Right(deployArgs) =>
              val runLocalDeploy =
                io.accur8.neodeploy.LocalDeploySubCommand(
                  deployArgs,
                )
              Layers.provide(
                runLocalDeploy.runM,
                config.some,
              )
            case Left(errorMsg) =>
              ZIO.fail(new RuntimeException(errorMsg))
          }
        },
      ).runT

    }

  }
  val gitignore = new Subcommand("gitignore") with Runner {

    descr("makes sure that .gitignore has the standard elements")

    override def runZ(main: Main) = zblock {
      main.runGitignore()
    }

  }

  val version_bump = new Subcommand("version_bump") with Runner {

    descr("upgrades the versions in version.properties aka runs a version bump")

    override def runZ(main: Main) = zblock {
      main.runVersionBump()
    }

  }

  addSubcommand(deploy)
  addSubcommand(resolve)
  addSubcommand(install)
  addSubcommand(buildDotSbt)
  addSubcommand(gitignore)
  addSubcommand(javaLauncherDotNix)
  addSubcommand(localDeploy)
  addSubcommand(localDeployDev)
  addSubcommand(promote)
  addSubcommand(version_bump)
  addSubcommand(validateServerAppConfigs)
  addSubcommand(setupDatabase)

  verify()

  override protected def onError(e: Throwable): Unit = {
    given CanEqual[Throwable,Throwable] = CanEqual.derived
    import exceptions._
    e match {
      case r: ScallopResult if !throwError.value =>
        r match {
          case Help("") =>
            builder.printHelp()
          case Help(subname) =>
            builder.findSubbuilder(subname).get.printHelp()
          case Version =>
            builder.vers.foreach(println)
          case ScallopException(message) =>
            errorMessageHandler(message)
          // following should never match, but just in case
          case other: exceptions.ScallopException =>
            errorMessageHandler(other.getMessage)
        }
      case e =>
        throw e
    }
  }

  def loadResolvedRepository(config: Option[Config] = None) = {
    val resolvedConfig = config.getOrElse(Config.default())
    Layers
      .resolvedRepositoryZ
      .provide(
        zl_succeed(resolvedConfig),
        LocalRootDirectory.layer,
        PathLocator.layer,
      )
  }

}
