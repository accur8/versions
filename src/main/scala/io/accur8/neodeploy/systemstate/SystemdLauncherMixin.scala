package io.accur8.neodeploy.systemstate


import io.accur8.neodeploy.Systemd
import io.accur8.neodeploy.Systemd.{TimerFile, UnitFile}
import io.accur8.neodeploy.model.{Launcher, SystemdDescriptor}
import io.accur8.neodeploy.resolvedmodel.ResolvedApp
import io.accur8.neodeploy.systemstate.SystemStateModel.M
import a8.shared.SharedImports.*
import io.accur8.neodeploy.model.Launcher.SystemdLauncher

trait SystemdLauncherMixin { self: SystemdLauncher =>

  lazy val unitName = resolvedApp.name.value

  /**
   * https://www.freedesktop.org/software/systemd/man/systemd.service.html
   * @param input
   * @return
   */
  override def installService: SystemState = {
    val input = resolvedApp
    val sd = systemdDescriptor
    val command = input.descriptor.install.command(input.descriptor, input.appDirectory, input.user.appsRootDirectory)
    Systemd.systemState(
      unitName = unitName,
      description = input.name.value,
      user = input.user,
      unitFile = UnitFile(
        Type = sd.`type`,
        environment = sd.environment,
        workingDirectory = command.workingDirectory.getOrElse(input.appDirectory),
        execStart = command.args.mkString(" "),
      ),
      timerFileOpt =
        sd.onCalendar.map { onCalendar =>
          TimerFile(
            onCalendar = onCalendar,
            persistent = sd.persistent,
          )
        },
      enableService = sd.enableService,
    )
  }

  override def serviceCommand(action: String) =
    systemdDescriptor.onCalendar match {
      case Some(_) =>
        None
      case None =>
        io.accur8.neodeploy.Overrides
          .userSystemCtlCommand
          .appendArgs(action, unitName)
          .copy(failOnNonZeroExitCode = false)
          .some
    }

}
