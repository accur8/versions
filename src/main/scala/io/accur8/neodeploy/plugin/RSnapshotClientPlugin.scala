package io.accur8.neodeploy.plugin

import io.accur8.neodeploy.SharedImports._
import io.accur8.neodeploy.model.RSnapshotClientDescriptor
import io.accur8.neodeploy.resolvedmodel.{ResolvedAuthorizedKey, ResolvedServer, ResolvedUser}
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemStateModel.M
import io.accur8.neodeploy.UserPlugin
import zio.Task

object RSnapshotClientPlugin extends UserPlugin.Factory[RSnapshotClientDescriptor]("rsnapshotClient")

case class RSnapshotClientPlugin(
  descriptor: RSnapshotClientDescriptor,
  user: ResolvedUser,
) extends UserPlugin {

  override def name: String = "rsnapshotClient"

  def descriptorJson = descriptor.toJsVal

  lazy val server: ResolvedServer = user.server

  // this makes sure there is a tab separate the include|exclude keyword and the path
  lazy val resolvedIncludeExcludeLines =
    descriptor
      .includeExcludeLines
      .map { line =>
        line
          .splitList("[ \t]", limit = 2)
          .mkString("\t")
      }
      .mkString("\n")

  lazy val sshUrl: String = z"${user.login}@${server.vpnName}"

  // this makes sure there is a tab separate the include|exclude keyword and the path
  lazy val resolvedBackupLines =
    descriptor
      .directories
      .map { directory =>
        val parts = Seq("backup", z"${sshUrl}:${directory}", z"${user.server.name}/")
        parts
          .mkString("\t")
      }
      .mkString("\n")


  override def resolveAuthorizedKeysImpl: N[Vector[ResolvedAuthorizedKey]] =
    user
      .server
      .repository
      .userPlugins
      .map {
        case rssp: RSnapshotServerPlugin =>
          rssp
            .user
            .resolveLoginKeys
        case _ =>
          zsucceed(None)
      }
      .sequence
      .map(_.flatten)

  def systemState(input: ResolvedUser): M[SystemState] =
    zsucceed(SystemState.Empty)
}

