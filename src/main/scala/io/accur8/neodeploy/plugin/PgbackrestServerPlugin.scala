package io.accur8.neodeploy.plugin

import a8.shared.SharedImports._
import io.accur8.neodeploy.Systemd.{TimerFile, UnitFile}
import io.accur8.neodeploy.model.{OnCalendarValue, PgbackrestServerDescriptor}
import io.accur8.neodeploy.resolvedmodel.{ResolvedAuthorizedKey, ResolvedServer, ResolvedUser}
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemStateModel.M
import io.accur8.neodeploy.{HealthchecksDotIo, Systemd, UserPlugin}
import zio.Task

object PgbackrestServerPlugin extends UserPlugin.Factory[PgbackrestServerDescriptor]("pgbackrestServer")

case class PgbackrestServerPlugin(
  descriptor: PgbackrestServerDescriptor,
  user: ResolvedUser,
) extends UserPlugin { resolvedServer =>

  override def name: String = "pgbackrestServer"

  def descriptorJson = descriptor.toJsVal

  override def resolveAuthorizedKeysImpl: Task[Vector[ResolvedAuthorizedKey]] =
    user
      .server
      .repository
      .userPlugins
      .map {
        case pgbcp: PgbackrestClientPlugin =>
          pgbcp.user.resolveLoginKeys
        case _ =>
          zsucceed(Vector.empty)
      }
      .sequence
      .map(_.flatten)

  lazy val server: ResolvedServer = user.server

  lazy val clients =
    user
      .server
      .repository
      .userPlugins
      .collect {
        case rc: PgbackrestClientPlugin =>
          rc
      }

  override def systemState(input: ResolvedUser): M[SystemState] =
    PgbackrestConfig.systemState(input)
      .map { configState =>
        input
          .plugins
          .pgbackrestServerOpt
          .map { pbs =>
            SystemState.Composite(
              "setup pgbackrest server",
              Vector(configState) ++ pbs.clients.map(c => clientState(c)),
            )
          }
          .getOrElse(SystemState.Empty)
      }


  def clientState(client: PgbackrestClientPlugin): SystemState = {

    val healthcheckState =
      SystemState.HealthCheck(
        HealthchecksDotIo.CheckUpsertRequest(
          name = z"pgbackrest-${client.server.name}",
          tags = z"pgbackrest managed ${client.server.name} active".some,
          timeout = 1.day.toSeconds.some,
          grace = 1.hours.toSeconds.some,
          unique = Iterable("name"),
        )
      )

    val systemdState =
      Systemd.systemState(
        z"pgbackrest-${client.server.name}",
        z"run daily pgbackrest from ${client.server.name} to this machine",
        resolvedServer.user,
        UnitFile(
          Type = "oneshot",
          workingDirectory = resolvedServer.user.home.absolutePath,
          execStart = z"/bootstrap/bin/run-pgbackrest ${client.stanzaName} ${client.server.name}",
        ),
        TimerFile(
          onCalendar = client.descriptor.onCalendar.getOrElse(OnCalendarValue.daily),
          persistent = true.some,
        ).some
      )

    SystemState.Composite(
      s"setup pgbackrest for ${client.server.name}",
      Vector(
        healthcheckState,
        systemdState,
      )
    )

  }
}
