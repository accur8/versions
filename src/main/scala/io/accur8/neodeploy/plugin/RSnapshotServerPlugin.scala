package io.accur8.neodeploy.plugin

import a8.shared.SharedImports._
import io.accur8.neodeploy.Systemd.{TimerFile, UnitFile}
import io.accur8.neodeploy.model.{OnCalendarValue, RSnapshotServerDescriptor}
import io.accur8.neodeploy.resolvedmodel.{ResolvedServer, ResolvedUser}
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemStateModel.M
import io.accur8.neodeploy.{HealthchecksDotIo, Systemd, UserPlugin}

object RSnapshotServerPlugin extends UserPlugin.Factory[RSnapshotServerDescriptor]("rsnapshotServer")

case class RSnapshotServerPlugin(
  descriptor: RSnapshotServerDescriptor,
  user: ResolvedUser,
) extends UserPlugin {

  override def name: String = s"rsnapshotServer-${user.qname}"

  def descriptorJson = descriptor.toJsVal

  lazy val server: ResolvedServer = user.server

  lazy val clients =
    user
      .server
      .repository
      .userPlugins
      .collect {
        case rcp: RSnapshotClientPlugin =>
          rcp
      }


  // for each rsnapshot client
  // create rsnapshot config
  // create healthchecks.io check
  // create systemd unit and timer

  // on each servers rsnapshot user create authorized_keys2 file entry
  // add proper scripts for ssh validations and invocation
  // add proper sudo implementation so we can sudo

  def systemState(user: ResolvedUser): M[SystemState] =
    zsucceed(rawSystemState(user))

  def rawSystemState(user: ResolvedUser): SystemState =
    user
      .plugins
      .resolvedRSnapshotServerOpt
      .map(_.systemState)
      .getOrElse(SystemState.Empty)


  def systemState: SystemState =
    SystemState.Composite(
      "setup rsnapshot server",
      clients
        .map { client =>
          setupClientSystemState(this, client)
        }
    )


  def setupClientSystemState(resolvedServer: RSnapshotServerPlugin, client: RSnapshotClientPlugin): SystemState = {

    lazy val rsnapshotConfigFile =
      resolvedServer
        .descriptor
        .configDir
        .file(z"rsnapshot-${client.server.name}.conf")

    lazy val configFileState =
      SystemState.TextFile(
        rsnapshotConfigFile,
        RSnapshotConfig.serverConfigForClient(resolvedServer, client),
      )

    val healthCheckState =
      SystemState.HealthCheck(
        HealthchecksDotIo.CheckUpsertRequest(
          name = z"rsnapshot-${client.server.name}",
          tags = z"rsnapshot managed ${client.server.name} active".some,
          timeout = 1.day.toSeconds.some,
          grace = 1.hours.toSeconds.some,
          unique = Iterable("name")
        )
      )

    val systemdState =
      Systemd.systemState(
        z"rsnapshot-${client.server.name}",
        z"run snapshot from ${client.server.name} to this machine",
        resolvedServer.user,
        UnitFile(
          Type = "oneshot",
          workingDirectory = rsnapshotConfigFile.parent,
          execStart = z"${user.home.absPath}/.nix-profile/bin/run-rsnapshot ${rsnapshotConfigFile.absPath} ${client.server.name}",
        ),
        TimerFile(
          onCalendar = OnCalendarValue.hourly,
          persistent = true.some,
        ).some
      )

    SystemState.Composite(
      z"setup rsnapshot for client ${client.server.name}",
      Vector(
        configFileState,
        healthCheckState,
        systemdState,
      )
    )

  }

}

