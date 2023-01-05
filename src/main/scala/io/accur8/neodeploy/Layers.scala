package io.accur8.neodeploy


import a8.shared.SharedImports._
import a8.shared.ZFileSystem
import a8.shared.app.LoggingF
import io.accur8.neodeploy.LocalUserSyncSubCommand.Config
import io.accur8.neodeploy.resolvedmodel.{ResolvedRepository, ResolvedServer}
import io.accur8.neodeploy.systemstate.SystemStateModel.{Environ, M, SystemStateLogger}
import zio.{Task, ZIO, ZLayer}

object Layers extends LoggingF {

  def provide[A](effect: ZIO[Environ, Throwable, A]): Task[A] =
    effect
      .provide(
        DnsService.layer,
        configL,
        healthchecksDotIoL,
        resolvedRepositoryL,
        resolvedUserL,
        resolvedServerL,
        SystemStateLogger.simpleLayer,
      )

  lazy val configFile =
    ZFileSystem
      .userHome
      .subdir(".a8")
      .file("server_app_sync.conf")

  lazy val configZ =
    configFile
      .readAsStringOpt
      .flatMap {
        case None =>
          val defaultConfig = Config.default()
          defaultConfig
            .gitRootDirectory
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

  lazy val configL = ZLayer.fromZIO(configZ)

  def resolvedRepositoryL =
    ZLayer.fromZIO(resolvedRepositoryZ)

  lazy val resolvedRepositoryZ =
    zservice[Config]
      .flatMap(config =>
        ResolvedRepository.loadFromDisk(config.gitRootDirectory)
      )

  def resolvedServerL =
    ZLayer.fromZIO(
      for {
        config <- zservice[Config]
        resolvedRepository <- zservice[ResolvedRepository]
        resolvedServer <-
          resolvedRepository
            .servers
            .find(s => s.name == config.serverName || s.descriptor.aliases.contains(config.serverName))
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
