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
import systemstate.SystemStateModel._



case class LocalUserSync(resolvedUser: ResolvedUser, syncsFilter: Filter[SyncName]) extends LoggingF {

  lazy val resolvedServer = resolvedUser.server

  lazy val stateDirectory: Directory =
    resolvedUser
      .home
      .subdir(".neodeploy-state")

  lazy val healthchecksDotIo = HealthchecksDotIo(resolvedServer.repository.descriptor.healthchecksApiToken)

  case class UserSync(previousStates: Vector[PreviousState]) extends SyncContainer[ResolvedUser, UserLogin](SyncContainer.Prefix("user"), stateDirectory) {

    override def filter(pair: NamePair): Boolean =
      syncsFilter.matches(pair.syncName)

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

  def userSyncRun: ApplyState[Unit] =
    SyncContainer.loadState(stateDirectory, SyncContainer.Prefix("user"))
      .flatMap { previousStates =>
        UserSync(previousStates)
          .run
      }

  def run: ApplyState[Unit] =
    for {
      _ <- loggerF.info(z"running for ${resolvedUser.qualifiedUserName}")
      _ <- loggerF.debug(z"resolved user ${resolvedUser.qualifiedUserName} -- ${resolvedUser.descriptor.prettyJson.indent("    ")}")
      _ <- loggerF.debug(z"resolved user plugins ${resolvedUser.qualifiedUserName} -- ${resolvedUser.plugins.descriptorJson.prettyJson.indent("    ")}")
      _ <- userSyncRun
    } yield ()

}