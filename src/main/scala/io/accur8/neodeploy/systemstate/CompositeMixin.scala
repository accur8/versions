package io.accur8.neodeploy.systemstate


import a8.shared.SharedImports._
import io.accur8.neodeploy.systemstate.SystemStateModel._

trait CompositeMixin extends SystemStateMixin { self: SystemState.Composite =>
  override def dryRunInstall: Vector[String] = Vector(description)
  override def isActionNeeded: M[Boolean] = zsucceed(false)
  override def stateKey: Option[StateKey] = None
  override def runApplyNewState = zunit
  override def runUninstallObsolete(interpreter: Interpreter) = zunit
}
