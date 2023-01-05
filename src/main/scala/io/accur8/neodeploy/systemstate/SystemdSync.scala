package io.accur8.neodeploy.systemstate


import io.accur8.neodeploy.{Sync, Systemd}
import io.accur8.neodeploy.Sync.SyncName
import io.accur8.neodeploy.Systemd.{TimerFile, UnitFile}
import io.accur8.neodeploy.model.{Launcher, SystemdDescriptor}
import io.accur8.neodeploy.resolvedmodel.ResolvedApp
import io.accur8.neodeploy.systemstate.SystemStateModel.M
import a8.shared.SharedImports._

case object SystemdSync extends Sync[ResolvedApp] {

  override val name: Sync.SyncName =
    SyncName("systemd")


  /**
   * https://www.freedesktop.org/software/systemd/man/systemd.service.html
   * @param input
   * @return
   */
  override def systemState(input: ResolvedApp): M[SystemState] =
    zsucceed(
      input.descriptor.launcher match {
        case sd: SystemdDescriptor =>
          val command = input.descriptor.install.command(input.descriptor, input.appDirectory, input.user.appsRootDirectory)
          Systemd.systemState(
            unitName = input.name.value,
            description = input.name.value,
            user = input.user,
            unitFile = UnitFile(
              Type = sd.`type`,
              environment = sd.environment,
              workingDirectory = command.workingDirectory.getOrElse(input.appDirectory).absolutePath,
              execStart = command.args.mkString(" "),
            ),
            timerFileOpt =
              sd.onCalendar.map { onCalendar =>
                TimerFile(
                  onCalendar = onCalendar,
                  persistent = sd.persistent,
                )
              },
          )
        case _ =>
          SystemState.Empty
      }
    )

}
