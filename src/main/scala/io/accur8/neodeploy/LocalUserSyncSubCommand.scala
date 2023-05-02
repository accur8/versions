package io.accur8.neodeploy


import a8.shared.SharedImports.{zservice, *}
import a8.shared.app.BootstrapConfig.UnifiedLogLevel
import a8.shared.{CompanionGen, FileSystem, ZFileSystem}
import a8.shared.app.{BootstrapConfig, BootstrappedIOApp, LoggerF}
import a8.shared.app.BootstrappedIOApp.BootstrapEnv
import io.accur8.neodeploy.MxLocalUserSyncSubCommand.*
import io.accur8.neodeploy.LocalUserSyncSubCommand.Config
import io.accur8.neodeploy.PushRemoteSyncSubCommand.Filter
import io.accur8.neodeploy.Sync.SyncName
import io.accur8.neodeploy.model.{ApplicationName, AppsRootDirectory, CaddyDirectory, DomainName, GitRootDirectory, GitServerDirectory, ServerName, SupervisorDirectory, UserLogin}
import io.accur8.neodeploy.resolvedmodel.{ResolvedRepository, ResolvedServer, ResolvedUser}
import zio.{ZIO, ZLayer}
import systemstate.SystemStateModel.*
import wvlet.log.{LogLevel, Logger}

import java.net.InetAddress

object LocalUserSyncSubCommand {

  object Config extends MxConfig {
    def default() =
      Config(
        GitRootDirectory(FileSystem.userHome.subdir("server-app-configs").asNioPath.toAbsolutePath.toString),
        ServerName.thisServer(),
        userLogin = UserLogin.thisUser(),
      )
  }

  @CompanionGen
  case class Config(
    gitRootDirectory: GitRootDirectory,
    serverName: ServerName,
    userLogin: UserLogin = UserLogin.thisUser(),
  )

}

case class LocalUserSyncSubCommand(syncsFilter: Filter[SyncName], wvletDefaultLogLevel: LogLevel) extends BootstrappedIOApp {

  override def defaultLogLevel =
    UnifiedLogLevel(wvletDefaultLogLevel)

  override def runT: ZIO[BootstrapEnv, Throwable, Unit] =
    Layers.provide(runM)

  def runM: M[Unit] =
    for {
      user <- zservice[ResolvedUser]
      _ <-
        LocalUserSync(user, syncsFilter)
          .run
    } yield ()

}
