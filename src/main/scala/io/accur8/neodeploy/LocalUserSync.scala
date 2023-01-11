package io.accur8.neodeploy


import a8.shared.{CompanionGen, StringValue}
import a8.shared.ZFileSystem.Directory
import io.accur8.neodeploy.model.{ApplicationDescriptor, ApplicationName, AppsRootDirectory, CaddyDirectory, DomainName, GitServerDirectory, Install, SupervisorDirectory, UserDescriptor, UserLogin}
import zio.{Task, UIO, ZIO}
import a8.shared.SharedImports._
import a8.shared.app.{Logging, LoggingF}
import a8.shared.json.ast
import a8.shared.json.ast.{JsDoc, JsObj, JsVal}
import io.accur8.neodeploy.PushRemoteSyncSubCommand.Filter
import io.accur8.neodeploy.Sync.SyncName
import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedServer, ResolvedUser}
import io.accur8.neodeploy.systemstate.SystemdSync
import systemstate.SystemStateModel._



case class LocalUserSync(resolvedUser: ResolvedUser, appsFilter: Filter[ApplicationName], syncsFilter: Filter[SyncName]) extends LoggingF {

  lazy val resolvedServer = resolvedUser.server

  lazy val stateDirectory: Directory =
    resolvedUser
      .home
      .subdir(".neodeploy-state")

  lazy val healthchecksDotIo = HealthchecksDotIo(resolvedServer.repository.descriptor.healthchecksApiToken)

  case class UserSync(previousStates: Vector[PreviousState]) extends SyncContainer[ResolvedUser, UserLogin](SyncContainer.Prefix("user"), stateDirectory, Filter.allowAll) {

    override def name(resolved: ResolvedUser): UserLogin = resolved.login
    override def nameFromStr(nameStr: String): UserLogin = UserLogin(nameStr)

    override val newResolveds = Vector(resolvedUser)

    override val staticSyncs: Seq[Sync[ResolvedUser]] =
      Seq(
        AuthorizedKeys2Sync,
        ManagedSshKeysSync,
      ).filter(s => syncsFilter.matches(s.name))

    override def resolvedSyncs(resolved: ResolvedUser): Seq[Sync[ResolvedUser]] =
      resolved.plugins.pluginInstances

  }

  case class AppSync(newResolveds: Vector[ResolvedApp], previousStates: Vector[PreviousState]) extends SyncContainer[ResolvedApp, ApplicationName](SyncContainer.Prefix("app"), stateDirectory, appsFilter) {

    override def name(resolved: ResolvedApp): ApplicationName = resolved.name
    override def nameFromStr(nameStr: String): ApplicationName = ApplicationName(nameStr)

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

  def appSyncRun: ZIO[Environ, Nothing, Either[Throwable, Unit]] = {
    loggerF.trace("appSyncRun") *> loggerF.debug("appSyncRun") *>
    SyncContainer.loadState(stateDirectory, SyncContainer.Prefix("app"))
      .either
      .flatMap {
        case Right(previousStates) =>
          AppSync(resolvedUser.resolvedApps, previousStates)
            .run
        case Left(th) =>
          zsucceed(Left(th))
      }
  }

  def userSyncRun: ZIO[Environ, Nothing, Either[Throwable, Unit]] =
    SyncContainer.loadState(stateDirectory, SyncContainer.Prefix("user"))
      .either
      .flatMap {
        case Right(previousStates) =>
          UserSync(previousStates)
            .run
        case Left(th) =>
          zsucceed(Left(th))
      }

  def run: ZIO[Environ, Nothing, Either[Throwable, Unit]] =
    for {
      _ <- loggerF.info(z"running for ${resolvedUser.qualifiedUserName}")
      _ <- loggerF.debug(z"resolved user ${resolvedUser.qualifiedUserName} -- ${resolvedUser.descriptor.prettyJson.indent("    ")}")
      _ <- loggerF.debug(z"resolved user plugins ${resolvedUser.qualifiedUserName} -- ${resolvedUser.plugins.descriptorJson.prettyJson.indent("    ")}")
      userResult <- userSyncRun
      result <-
        userResult match {
          case Right(_) =>
            appSyncRun
          case l =>
            zsucceed(l)
        }
    } yield result

}