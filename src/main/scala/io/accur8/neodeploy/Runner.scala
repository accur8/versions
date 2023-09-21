package io.accur8.neodeploy


import a8.shared.app.BootstrapConfig.TempDir
import a8.shared.app.{BootstrapConfig, BootstrappedIOApp}
import a8.shared.app.BootstrappedIOApp.BootstrapEnv
import a8.versions.apps.Main
import io.accur8.neodeploy.model.{ApplicationName, LocalRootDirectory, ServerName, UserLogin}
import io.accur8.neodeploy.resolvedmodel.ResolvedRepository
import zio.{Scope, Task, ZIO}
import LocalDeploy.Config
import io.accur8.neodeploy.systemstate.SystemStateModel.PathLocator
import SharedImports._

case class Runner(
//  serversFilter: Filter[ServerName] = Filter.allowAll,
//  usersFilter: Filter[UserLogin] = Filter.allowAll,
//  appsFilter: Filter[ApplicationName] = Filter.allowAll,
//  syncsFilter: Filter[SyncName] = Filter.allowAll,

  config: Option[Config] = None,
  remoteDebug: Boolean = false,
  remoteTrace: Boolean = false,
  runnerFn: (ResolvedRepository, Runner) => zio.ZIO[zio.Scope, Throwable, Unit],
)
  extends BootstrappedIOApp
{

  def unsafeRun(): Unit =
    main(Array[String]())

  override def runT: ZIO[BootstrapEnv, Throwable, Unit] = {
    val resolvedConfig = config.getOrElse(Config.default())
    val rawEffect: ZIO[Scope, Throwable, Unit] =
      for {
        resolvedRepository <-
          Layers
            .resolvedRepositoryZ
            .provide(
              zl_succeed(resolvedConfig),
              LocalRootDirectory.layer,
              PathLocator.layer,
            )
        _ <- runnerFn(resolvedRepository, this)
      } yield ()
    Layers.provide(rawEffect.scoped, resolvedConfig.some)
  }

}