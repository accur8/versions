package io.accur8.neodeploy

import a8.shared.ZFileSystem.{Directory, File}
import a8.shared.SharedImports.*
import a8.shared.ZString
import a8.shared.ZString.ZStringer
import io.accur8.neodeploy.model.Launcher.DockerLauncher
import io.accur8.neodeploy.model.{DockerDescriptor, SupervisorDescriptor, SupervisorDirectory}
import io.accur8.neodeploy.resolvedmodel.ResolvedApp
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemStateModel.M

trait DockerLauncherMixin { self: DockerLauncher =>

  def installService: SystemState =
    SystemState.DockerState(dockerDescriptor)

  def serviceCommand(action: String) =
    Overrides
      .sudoDockerCommand
      .appendArgs("stop", resolvedApp.name.value)
      .some


}
