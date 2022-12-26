package io.accur8.neodeploy


import a8.shared.ZFileSystem.Directory
import a8.shared.SharedImports._
import io.accur8.neodeploy.resolvedmodel.ResolvedUser
import zio.ZIO
import PredefAssist._
import a8.shared.{CompanionGen, FileSystem}
import io.accur8.neodeploy.MxSystemd.MxUnitFile
import io.accur8.neodeploy.model.OnCalendarValue
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemState.{RunCommandState, TriggeredState, jsonCodec}
import io.accur8.neodeploy.systemstate.SystemStateModel._

/**
 * user level systemd service and timers
 *        https://gist.github.com/oprypin/0f0c3479ab53e00988b52919e5d7c144
 *        https://opensource.com/article/20/7/systemd-timers
 *
 *  a good systemd reference
 *        https://www.digitalocean.com/community/tutorials/understanding-systemd-units-and-unit-files
 */
object Systemd {

  object UnitFile extends MxUnitFile
  @CompanionGen
  case class UnitFile(
    Type: String = "simple",
    environment: Vector[String] = Vector.empty,
    workingDirectory: String,
    execStart: String,
  )

  case class TimerFile(
    onCalendar: OnCalendarValue,
    persistent: Option[Boolean] = None,
  ) {
    def timerLines = (
      Some(z"OnCalendar=${onCalendar}")
        ++ persistent.map(p => z"Persistent=${p}")
    )
  }

  /**
   * from here https://wiki.archlinux.org/title/systemd/User#Automatic_start-up_of_systemd_user_instances
   */
  def systemState(
    unitName: String,
    description: String,
    user: ResolvedUser,
    unitFile: UnitFile,
    timerFileOpt: Option[TimerFile] = None,
  ): SystemState = {

    val directory = user.home.subdir(z".config/systemd/user")

    val unitFileContents =
      z"""
         |[Unit]
         |Description=$description
         |${unitFile.environment.map("Environment=" + _).mkString("\n")}
         |
         |[Service]
         |Type=${unitFile.Type}
         |WorkingDirectory=${unitFile.workingDirectory}
         |StandardOutput=journal
         |ExecStart=${unitFile.execStart}
         |
         |[Install]
         |WantedBy=multi-user.target
         |""".stripMargin.ltrim

    val unitFileState =
      SystemState.TextFile(
        file = directory.file(z"${unitName}.service"),
        contents = unitFileContents,
      )

    val timerFileStateOpt =
      timerFileOpt.map {timerFile =>

        val timerFileContents =
          z"""
             |[Unit]
             |Description=${description}
             |
             |[Timer]
             |${timerFile.timerLines.mkString("\n")}
             |
             |[Install]
             |WantedBy=timers.target
          """.stripMargin.ltrim

        SystemState.TextFile(
          file = directory.file(z"${unitName}.timer"),
          contents = timerFileContents,
        )
      }


    val daemonReloadCommand =
      Overrides.userSystemCtlCommand
        .appendArgs("--user", "daemon-reload")
        .asSystemStateCommand

    val enableTimerCommand =
      Overrides.userSystemCtlCommand
        .appendArgs("--user", "enable", "--now", z"${unitName}.timer")
        .asSystemStateCommand

    val disableTimerCommand =
      Overrides.userSystemCtlCommand
        .appendArgs("--user", "disable", z"${unitName}.timer")
        .asSystemStateCommand

    val stopTimerCommand =
      Overrides.userSystemCtlCommand
        .appendArgs("--user", "stop", z"${unitName}.timer")
        .asSystemStateCommand

    val enableTimerCommands =
      timerFileOpt
        .toVector
        .flatMap(_ =>
          Vector(enableTimerCommand)
        )

    val uninstallTimerCommands =
      timerFileOpt
        .toVector
        .flatMap(_ =>
          Vector(stopTimerCommand, disableTimerCommand)
        )

    val enableUserLingerCommand =
      Overrides.userLoginCtlCommand
        .appendArgs("enable-linger")
        .asSystemStateCommand

    val manageSystemdUnitState: SystemState =
      RunCommandState(
        StateKey("enable/disable systemd", unitName).some,
        installCommands = Vector(daemonReloadCommand) ++ enableTimerCommands ++ Vector(enableUserLingerCommand),
        uninstallCommands = uninstallTimerCommands,
      )

    val unitFiles = Vector(unitFileState) ++ timerFileStateOpt

    SystemState.Composite(
      description = s"systemd unit ${unitName}",
      states = Vector(
        TriggeredState(
          triggerState =
            SystemState.Composite(
              description = "install unit files",
              states = unitFiles,
            ),
          postTriggerState = manageSystemdUnitState,
        )
      )
    )

  }

}
