package io.accur8.neodeploy

import a8.shared.SharedImports.*
import a8.shared.ZFileSystem.Directory
import a8.shared.app.{Logging, LoggingF}
import a8.shared.json.ast
import a8.shared.json.ast.{JsDoc, JsObj, JsVal}
import a8.shared.{CompanionGen, StringValue}
import io.accur8.neodeploy.PushRemoteSyncSubCommand.Filter
import io.accur8.neodeploy.Sync.SyncName
import io.accur8.neodeploy.model.*
import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedServer, ResolvedUser}
import io.accur8.neodeploy.systemstate.SystemStateModel.*
import io.accur8.neodeploy.systemstate.SystemdSync
import zio.{Task, UIO, ZIO}


case class LocalAppSync(resolvedUser: ResolvedUser, appsFilter: Filter[ApplicationName], syncsFilter: Filter[SyncName]) extends LoggingF {

  lazy val resolvedServer = resolvedUser.server

  lazy val stateDirectory: Directory =
    resolvedUser
      .home
      .subdir(".neodeploy-state")

  lazy val healthchecksDotIo = HealthchecksDotIo(resolvedServer.repository.descriptor.healthchecksApiToken)

  case class AppSync(newResolveds: Vector[ResolvedApp], previousStates: Vector[PreviousState]) extends SyncContainer[ResolvedApp, ApplicationName](SyncContainer.Prefix("app"), stateDirectory) {

    override def name(resolved: ResolvedApp): ApplicationName = resolved.name
    override def nameFromStr(nameStr: String): ApplicationName = ApplicationName(nameStr)

    override def filter(pair: NamePair): Boolean =
      syncsFilter.matches(pair.syncName) && appsFilter.matches(pair.resolvedName)

    override val staticSyncs: Seq[Sync[ResolvedApp]] =
      Vector(
        SupervisorSync(resolvedServer.supervisorDirectory),
        ApplicationInstallSync(resolvedUser.appsRootDirectory),
        DockerSync,
        SystemdSync,
      ).filter(s => syncsFilter.include(s.name))

    override def resolvedSyncs(resolved: ResolvedApp): Seq[Sync[ResolvedApp]] =
      Seq.empty

  }

  def appSyncRun: ZIO[Environ, Throwable, Unit] =
    loggerF.trace("appSyncRun") *> loggerF.debug("appSyncRun") *>
    SyncContainer.loadState(stateDirectory, SyncContainer.Prefix("app"))
      .flatMap { previousStates =>
        AppSync(resolvedUser.resolvedApps, previousStates)
          .run
      }

  def run: ZIO[Environ, Throwable, Unit] =
    for {
      _ <- loggerF.info(z"running for ${resolvedUser.qualifiedUserName}")
      _ <- loggerF.debug(z"resolved user ${resolvedUser.qualifiedUserName} -- ${resolvedUser.descriptor.prettyJson.indent("    ")}")
      _ <- loggerF.debug(z"resolved user plugins ${resolvedUser.qualifiedUserName} -- ${resolvedUser.plugins.descriptorJson.prettyJson.indent("    ")}")
      _ <- appSyncRun
    } yield ()

}