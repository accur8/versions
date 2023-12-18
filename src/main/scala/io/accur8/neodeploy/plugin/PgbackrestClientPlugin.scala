package io.accur8.neodeploy.plugin

import io.accur8.neodeploy.SharedImports._
import io.accur8.neodeploy
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

  override def name: String = "pgbackrestClient"

  def stanzaName = descriptor.stanzaNameOverride.getOrElse(user.server.name.value)

  def descriptorJson = descriptor.toJsVal

  def resolvedServer: PgbackrestServerPlugin =
    user
      .server
      .repository
      .resolvedPgbackrestServerOpt
      .getOrError("must have a pgbackrest server configured")


  override def resolveAuthorizedKeysImpl: N[Vector[ResolvedAuthorizedKey]] =
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

  def systemState(input: ResolvedUser): M[SystemState] =
    PgbackrestConfig.systemState(input)

}

