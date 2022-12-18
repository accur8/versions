package io.accur8.neodeploy


import a8.shared.app.BootstrappedIOApp
import a8.shared.app.BootstrappedIOApp.BootstrapEnv
import a8.versions.apps.Main
import io.accur8.neodeploy.PushRemoteSyncSubCommand.Filter
import io.accur8.neodeploy.Sync.SyncName
import io.accur8.neodeploy.model.{ApplicationName, ServerName, UserLogin}
import io.accur8.neodeploy.resolvedmodel.ResolvedRepository
import zio.{Task, ZIO}

case class Runner(
  serversFilter: Filter[ServerName] = Filter.allowAll,
  usersFilter: Filter[UserLogin] = Filter.allowAll,
  appsFilter: Filter[ApplicationName] = Filter.allowAll,
  syncsFilter: Filter[SyncName] = Filter.allowAll,
  remoteDebug: Boolean = false,
  remoteTrace: Boolean = false,
  runnerFn: (ResolvedRepository, Runner) => Task[Unit],
)
  extends BootstrappedIOApp
{

  def unsafeRun(): Unit =
    main(Array[String]())

  override def runT: ZIO[BootstrapEnv, Throwable, Unit] =
    Layers.resolvedRepositoryZ
      .flatMap(resolvedRepository =>
        runnerFn(resolvedRepository, this)
      )
      .provide(Layers.configL)
}