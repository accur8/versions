package io.accur8.neodeploy.systemstate

import SystemStateModel._

trait SystemStateMixin {

  def stateKey: Option[StateKey]

  def dryRunInstall: Vector[String]
  def dryRunUninstall(interpreter: Interpreter): Vector[String] =
    dryRunInstall.map("uninstall " + _)
  
  def isActionNeeded: M[Boolean]

  /**
   * applies the state for just this system state and no sub states
   */
  def runApplyNewState: M[Unit]

  /**
   * uninstalls the state for just this system state and no sub states
   */
  def runUninstallObsolete(interpreter: Interpreter): M[Unit]

}

