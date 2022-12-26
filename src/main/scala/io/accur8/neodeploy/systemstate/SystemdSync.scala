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


  override def systemState(input: ResolvedApp): M[SystemState] =
    zsucceed(
      input.descriptor.launcher match {
        case sd: SystemdDescriptor =>
          Systemd.systemState(
            unitName = input.name.value,
            description = input.name.value,
            user = input.user,
            unitFile = UnitFile(
              Type = sd.Type,
              environment = sd.environment,
              workingDirectory = input.appDirectory.absolutePath,
              execStart = input.descriptor.install.execArgs(input.descriptor, input.appDirectory, input.user.appsRootDirectory).mkString(" "),
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
