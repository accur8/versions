package io.accur8.neodeploy.systemstate


import a8.shared.SharedImports._
import a8.common.logging.LoggingF
import io.accur8.neodeploy.Overrides
import io.accur8.neodeploy.model.DockerDescriptor.UninstallAction
import io.accur8.neodeploy.systemstate.SystemStateModel.{M, StateKey}

trait DockerStateMixin extends SystemStateMixin with LoggingF { self: SystemState.DockerState =>

  // sudo docker ps -a --format {{.Names}}

  override def stateKey: Option[SystemStateModel.StateKey] =
    StateKey("docker", descriptor.name).some

  override def dryRunInstall: Vector[String] =
    Vector(s"docker ${descriptor.name}")

  override def isActionNeeded: M[Boolean] =
    isContainerInstalled
      .map(!_)
//      .tap(v => loggerF.debug(s"docker ${descriptor.name} isActionNeeded = ${v}"))

  def isContainerInstalled: M[Boolean] =
    Overrides.sudoDockerCommand
      .appendArgs("ps", "-a", "--format", "{{.Names}}")
      .execLogOutput
      .map { output =>
        output
          .outputLines
          .map(_.trim)
          .filter(_.toLowerCase === descriptor.name.toLowerCase)
          .nonEmpty
      }

  /**
   * applies the state for just this system state and no sub states
   */
  override def runApplyNewState: M[Unit] =
    runDockerRun

  /**
   * uninstalls the state for just this system state and no sub states
   */
  override def runUninstallObsolete(interpreter: Interpreter): M[Unit] =
    descriptor.uninstallAction match {
      case UninstallAction.Stop =>
        runDockerStop
      case UninstallAction.Remove =>
        runDockerStop *> runDockerRemove
    }

  def runDockerStop: M[Unit] =
    Overrides.sudoDockerCommand
      .appendArgs("stop", descriptor.name)
      .execCaptureOutput
      .as(())

  def runDockerRemove: M[Unit] =
    Overrides.sudoDockerCommand
      .appendArgs("rm", descriptor.name)
      .execCaptureOutput
      .as(())

  def runDockerRun =
    Overrides.sudoDockerCommand
      .appendArgs("run", "-d", "--name", descriptor.name)
      .appendArgsSeq(descriptor.args)
      .execCaptureOutput
      .as(())

}
