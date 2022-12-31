package io.accur8.neodeploy.plugin

import a8.shared.SharedImports._
import io.accur8.neodeploy
import io.accur8.neodeploy.Sync.SyncName
import io.accur8.neodeploy.model.PgbackrestClientDescriptor
import io.accur8.neodeploy.resolvedmodel.{ResolvedAuthorizedKey, ResolvedServer, ResolvedUser}
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemStateModel.M
import io.accur8.neodeploy.UserPlugin
import zio.Task

object PgbackrestClientPlugin extends UserPlugin.Factory[PgbackrestClientDescriptor]("pgbackrestClient")

case class PgbackrestClientPlugin(
  descriptor: PgbackrestClientDescriptor,
  user: ResolvedUser,
) extends UserPlugin {

  def stanzaName = descriptor.stanzaNameOverride.getOrElse(user.server.name.value)

  def descriptorJson = descriptor.toJsVal

  val name = SyncName("pgbackrestClient")

  def resolvedServer: PgbackrestServerPlugin =
    user
      .server
      .repository
      .resolvedPgbackrestServerOpt
      .getOrError("must have a pgbackrest server configured")


  override def resolveAuthorizedKeysImpl: Task[Vector[ResolvedAuthorizedKey]] =
    user
      .server
      .repository
      .userPlugins
      .map {
        case rps: PgbackrestServerPlugin =>
          rps.user.resolveLoginKeys
        case _ =>
          zsucceed(Vector.empty)
      }
      .sequence
      .map(_.flatten)

  lazy val server: ResolvedServer = user.server

  override def systemState(input: ResolvedUser): M[SystemState] =
    PgbackrestConfig.systemState(input)

}

