package io.accur8.neodeploy


import a8.shared.SharedImports.*
import a8.shared.ZFileSystem
import a8.common.logging.LoggingF
import io.accur8.neodeploy.LocalDeploy.Config
import io.accur8.neodeploy.model.{AppsInfo, CaddyDirectory, GitRootDirectory, LocalRootDirectory, UserLogin}
import io.accur8.neodeploy.resolvedmodel.{ResolvedRepository, ResolvedServer}
import io.accur8.neodeploy.systemstate.SystemStateModel.{Environ, M, PathLocator, RunTimestamp, SystemStateLogger}
import zio.{Task, ZIO, ZLayer}

object Layers extends LoggingF {

  import a8.shared.ZFileSystem.SymlinkHandlerDefaults.follow

  type N[A] = zio.ZIO[PathLocator, Throwable, A]

  def provideN[A](effect: N[A], rootDirectory: LocalRootDirectory = LocalRootDirectory.default): Task[A] =
    effect
      .provide(
        PathLocator.layer,
        zl_succeed(rootDirectory),
      )

  def provide[A](effect: M[A], configOverride: Option[Config] = None): Task[A] = {
    val resolvedConfigL: ZLayer[Any, Throwable, Config] =
      configOverride match {
        case None =>
          configL
        case Some(c) =>
          ZLayer.succeed(c)
      }
    zio.ZIO.scoped(effect)
      .provide(
        CaddyDirectory.layer,
        DnsService.layer,
        resolvedConfigL,
        healthchecksDotIoL,
        resolvedRepositoryL,
//        resolvedUserL,
//        resolvedServerL,
        userLoginL,
        SystemStateLogger.simpleLayer,
        RunTimestamp.layer,
//        AppsInfo.layer,
        PathLocator.layer,
        LocalRootDirectory.layer,
      )
  }

  lazy val configFile =
    ZFileSystem
      .userHome
      .subdir(".a8")
      .file("server_app_sync.conf")

  lazy val configZ: Task[Config] =
    configFile
      .readAsStringOpt
      .flatMap {
        case None =>
          val defaultConfig = Config.default
          ZFileSystem
            .dir(defaultConfig.gitRootDirectory.value)
            .exists
            .flatMap {
              case true =>
                zsucceed(defaultConfig)
              case false =>
                zfail(new RuntimeException(s"tried using default config ${defaultConfig} but ${defaultConfig.gitRootDirectory} does not exist"))
            }
        case Some(jsonStr) =>
          json.readF[Config](jsonStr)
            .mapError { e =>
              val msg = s"error reading ${configFile}"
              logger.warn(msg, e)
              throw new RuntimeException(msg, e)
            }
      }

  lazy val configL: ZLayer[Any, Throwable, Config] = ZLayer.fromZIO(configZ)

  def resolvedRepositoryL =
    ZLayer.fromZIO(resolvedRepositoryZ)

  lazy val resolvedRepositoryZ =
    zservice[Config]
      .flatMap(config =>
        ResolvedRepository.loadFromDisk(config.gitRootDirectory)
      )

  def userLoginL: ZLayer[Config, Throwable, UserLogin] =
    ZLayer.service[Config].project(_.userLogin)

  def resolvedServerL =
    ZLayer.fromZIO(
      for {
        config <- zservice[Config]
        resolvedRepository <- zservice[ResolvedRepository]
        resolvedServer <-
          resolvedRepository
            .serversByName
            .get(config.serverName)
            .map(zsucceed)
            .getOrElse(zfail(new RuntimeException(s"server ${config.serverName} not found")))
      } yield resolvedServer
    )

  lazy val resolvedUserL =
    ZLayer.fromZIO(
      for {
        config <- zservice[Config]
        resolvedServer <- zservice[ResolvedServer]
        user <- resolvedServer.fetchUserZ(config.userLogin)
      } yield user
    )

  lazy val healthchecksDotIoL =
    ZLayer.fromZIO(
      zservice[ResolvedRepository]
        .map(_.descriptor.healthchecksApiToken)
        .map(HealthchecksDotIo.apply)
    )

}
