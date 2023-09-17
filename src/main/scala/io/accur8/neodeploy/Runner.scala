package io.accur8.neodeploy


import a8.shared.app.BootstrapConfig.TempDir
import a8.shared.app.{BootstrapConfig, BootstrappedIOApp}
import a8.shared.app.BootstrappedIOApp.BootstrapEnv
import a8.versions.apps.Main
import io.accur8.neodeploy.model.{ApplicationName, ServerName, UserLogin}
import io.accur8.neodeploy.resolvedmodel.ResolvedRepository
import zio.{Task, ZIO}

object Runner {
  type M[A] = zio.ZIO[zio.Scope & TempDir, Throwable, A]
}

case class Runner(
//  serversFilter: Filter[ServerName] = Filter.allowAll,
//  usersFilter: Filter[UserLogin] = Filter.allowAll,
//  appsFilter: Filter[ApplicationName] = Filter.allowAll,
//  syncsFilter: Filter[SyncName] = Filter.allowAll,
  remoteDebug: Boolean = false,
  remoteTrace: Boolean = false,
  runnerFn: (ResolvedRepository, Runner) => Runner.M[Unit],
)
  extends BootstrappedIOApp
{

  def unsafeRun(): Unit =
    main(Array[String]())

  override def runT: ZIO[BootstrapEnv, Throwable, Unit] = {
//    ZIO.attemptBlocking(wvlet.log.Logger.setDefaultLogLevel(defaultLogLevel)) *>
    Layers.resolvedRepositoryZ
      .flatMap(resolvedRepository =>
        runnerFn(resolvedRepository, this)
      )
      .provideSome[zio.Scope & TempDir](Layers.configL)
  }
}