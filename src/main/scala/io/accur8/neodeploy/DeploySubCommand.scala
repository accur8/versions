package io.accur8.neodeploy


import a8.shared.{Exec, StringValue}
import io.accur8.neodeploy.model.{ApplicationName, DomainName, GitRootDirectory, Install, ServerName, UserLogin, Version}
import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedRepository, ResolvedUser}
import zio.ZIO
import a8.shared.SharedImports.*
import a8.common.logging.LoggingF
import a8.versions.{ParsedVersion, RepositoryOps, VersionParser}
import a8.versions.model.{RepoPrefix, ResolutionRequest}
import io.accur8.neodeploy.DeploySubCommand.DeployAppEffects
import io.accur8.neodeploy.DeployUser.{InfraUser, RegularUser}
import io.accur8.neodeploy.Deployable.{InfraStructureDeployable, ServerDeployable, UserDeployable}
import io.accur8.neodeploy.Layers.N
import io.accur8.neodeploy.LocalDeploy.Config
import io.accur8.neodeploy.systemstate.SystemState.JavaAppInstall
import io.accur8.neodeploy.systemstate.SystemStateModel.{Command, Environ, M}
import org.rogach.scallop.{ArgType, ScallopOption, ValueConverter}
import io.accur8.neodeploy.systemstate.SystemStateModel

object DeploySubCommand {

  case class DeployAppEffects(
    appDeploy: AppDeploy,
    version: Version,
    successEffect: N[Unit],
    errorEffect: N[Unit],
  )

}

case class DeploySubCommand(
  resolvedRepository: ResolvedRepository,
  runner: Runner,
  deployArgs: ResolvedDeployArgs,
)
  extends LoggingF
{

  lazy val deployArgsByUser: Map[DeployUser, Iterable[DeployArg]] =
    deployArgs
      .args
      .flatMap(da => da.deployUsers.map(_ -> da))
      .groupBy(_._1)
      .map(t => t._1 -> t._2.map(_._2))

  def run: N[Unit] = {
    for {
      deployResults <-
        deployArgsByUser
          .map(e => runDeploy(e._1, e._2))
          .sequence
      _ <- gitCommit(deployResults)
      _ <- gitPush
    } yield ()
  }

  def resolveVersion(appDeploy: AppDeploy): N[Version] = {
    import appDeploy.resolvedApp
    appDeploy.version.map(_.value).getOrElse("current").toLowerCase.trim match {
      case v: ("latest" | "current") =>
        (resolvedApp.loadedApplicationDescriptor.descriptor.install, v) match {
          case (ja: Install.JavaApp, "current") =>
            zsucceed(ja.version)
          case (ja: Install.JavaApp, "latest") =>
            zblock {
              val parsedVersion = ParsedVersion.parse(ja.version.value).get
              val resolutionRequest =
                ResolutionRequest(
                  repoPrefix = ja.repository.getOrElse(RepositoryOps.default.repoConfigPrefix),
                  organization = ja.organization,
                  artifact = ja.artifact,
                  version = Version("latest"),
                  branch = parsedVersion.buildInfo.get.branch.some,
                )
              val resolutionResponse = RepositoryOps.runResolve(resolutionRequest)
              resolutionResponse.version
            }
          case _ =>
            zfail(new RuntimeException(s"${v} is only valid on java apps not ${resolvedApp.loadedApplicationDescriptor.descriptor.install}"))
        }
      case _ =>
        zsucceed(appDeploy.version.get)
    }
  }

  def gitCommit(deployResults: Iterable[DeployResult]): N[Unit] =
    zblock {
      val appVersions = deployResults.flatMap(_.appVersions)
      val versionInfo =
        if ( appVersions.nonEmpty ) {
          appVersions
            .map(t => s"${t._1.value} -> ${t._2.value}").mkString("\n","\n","").indent("        ")
        } else {
          ""
        }
      Command("git", "commit", "-am", z"deploy ${deployArgs.asCommandLineArgs.mkString(" ")}${versionInfo}")
        .inDirectory(resolvedRepository.gitRootDirectory.unresolved)
        .execInline(): @scala.annotation.nowarn
    }

  def gitPush: N[Unit] =
    zblock(
      Command("git", "push")
        .inDirectory(resolvedRepository.gitRootDirectory.unresolved)
        .execInline(): @scala.annotation.nowarn
    )

  def prepareDeployArgs(args: Iterable[DeployArg], deployAppEffects: Iterable[DeployAppEffects]): Iterable[String] = {
    val nonAppArgs =
      args.flatMap {
        case a: AppDeploy => None
        case a => Some(a.originalArg.originalValue)
      }
    val appArgs =
      deployAppEffects
        .map(dae => s"${dae.appDeploy.resolvedApp.name.value}:${dae.version.value}")
    nonAppArgs ++ appArgs
  }

  def runDeploy(deployUser: DeployUser, args: Iterable[DeployArg]): N[DeployResult] = {
    deployUser match {
      case InfraUser(_) =>
        runInfraDeploy(args)
      case ru: RegularUser =>
        runDeploy(ru, args)
    }
  }

  def runInfraDeploy(args: Iterable[DeployArg]): N[DeployResult] = {
    val deployUser = InfraUser(resolvedRepository)
    val effect: M[DeployResult] =
      for {
        gitRootDirectory <- zservice[Config].map(_.gitRootDirectory)
        _ <- SyncContainer(gitRootDirectory.subdir(".state/infra"), deployUser, deployArgs.args.toVector).run
      } yield DeployResult(deployUser)
    Layers.provide(effect)
  }

  def runDeploy(deployUser: RegularUser, args: Iterable[DeployArg]): N[DeployResult] = {

    val resolvedUser = deployUser.user

    val deployAppEffects: N[Iterable[DeployAppEffects]] =
      args
        .collect { case a: AppDeploy => a }
        .map(deployAppEffectsT)
        .sequence

    args
      .collect { case a: AppDeploy => a }
      .map(deployAppEffectsT)
      .sequence
      .flatMap { deployAppEffects =>

        val happyPathEffect: N[DeployResult] =
          PushRemoteDeploy(
            resolvedRepository,
            runner,
            resolvedUser,
            prepareDeployArgs(args, deployAppEffects),
          ).run.map { _ =>
            DeployResult(
              deployUser,
              deployAppEffects.map(dae => dae.appDeploy.resolvedApp.name -> dae.version),
            )
          }

        happyPathEffect
          .onError(_ =>
            deployAppEffects
              .map(_.errorEffect)
              .sequence
              .logVoid
          )
      }

  }

  def deployAppEffectsT(appDeploy: AppDeploy): N[DeployAppEffects] = {

    val app = appDeploy.resolvedApp

    val resolvedRunner = runner

    val versionDotPropsFile = app.gitDirectory.file("version.properties")

    for {
      version <- resolveVersion(appDeploy)
      savedVersionDotProps <- versionDotPropsFile.readAsStringOpt
    } yield {

      // set the version
      val setVersionEffect =
        versionDotPropsFile
          .write(z"""# ${a8.shared.FileSystem.fileSystemCompatibleTimestamp()} -- rawVersion is ${version.value}${"\n"}version_override=${version}""")

      val revertEffect =
        savedVersionDotProps match {
          case None =>
            versionDotPropsFile.delete
          case Some(content) =>
            versionDotPropsFile.write(content)
        }
      val onErrorEffect =
        for {
          _ <- loggerF.info(s"deploy failed reverting ${versionDotPropsFile}")
          _ <- revertEffect.logVoid
        } yield ()

      DeployAppEffects(
        appDeploy,
        version,
        setVersionEffect,
        onErrorEffect,
      )

    }

  }

}
