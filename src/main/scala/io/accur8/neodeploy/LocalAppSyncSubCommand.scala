package io.accur8.neodeploy

import a8.shared.SharedImports.{zservice, *}
import a8.shared.app.BootstrapConfig.UnifiedLogLevel
import a8.shared.app.BootstrappedIOApp.BootstrapEnv
import a8.shared.app.{BootstrapConfig, BootstrappedIOApp, LoggerF}
import a8.shared.{CompanionGen, FileSystem, ZFileSystem}
import io.accur8.neodeploy.LocalUserSyncSubCommand.Config
import io.accur8.neodeploy.MxLocalUserSyncSubCommand.*
import io.accur8.neodeploy.PushRemoteSyncSubCommand.Filter
import io.accur8.neodeploy.Sync.SyncName
import io.accur8.neodeploy.model.*
import io.accur8.neodeploy.resolvedmodel.{ResolvedRepository, ResolvedServer, ResolvedUser}
import io.accur8.neodeploy.systemstate.SystemStateModel.*
import wvlet.log.{LogLevel, Logger}
import zio.{ZIO, ZLayer}

import java.net.InetAddress

object LocalAppSyncSubCommand {

}

case class LocalAppSyncSubCommand(appsFilter: Filter[ApplicationName], syncsFilter: Filter[SyncName], wvletDefaultLogLevel: LogLevel) extends BootstrappedIOApp {

  override def defaultLogLevel =
    UnifiedLogLevel(wvletDefaultLogLevel)

  override def runT: ZIO[BootstrapEnv, Throwable, Unit] =
    Layers.provide(runM)

  def runM: M[Unit] =
    for {
      user <- zservice[ResolvedUser]
      _ <-
        LocalAppSync(user, appsFilter, syncsFilter)
          .run
    } yield ()

}
