package io.accur8.neodeploy.systemstate

import a8.shared.SharedImports._
import io.accur8.neodeploy.systemstate.SystemStateModel.M

trait TriggeredStateMixin extends SystemStateMixin { self: SystemState.TriggeredState =>

  override def stateKey: Option[SystemStateModel.StateKey] = None

  override def dryRunInstall: Vector[String] = Vector.empty

  override def isActionNeeded: M[Boolean] =
    zsucceed(false)

  /**
   * applies the state for just this system state and no sub states
   */
  override def runApplyNewState: M[Unit] =
    zunit

  /**
   * uninstalls the state for just this system state and no sub states
   */
  override def runUninstallObsolete(interpreter: Interpreter): M[Unit] =
    zunit

}
