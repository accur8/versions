package io.accur8.neodeploy.systemstate


import io.accur8.neodeploy.systemstate.SystemStateModel.M
import a8.shared.SharedImports._
import io.accur8.neodeploy.DnsService

trait DnsRecordMixin extends SystemStateMixin { self: SystemState.DnsRecord =>

  override def stateKey: Option[SystemStateModel.StateKey] =
    SystemStateModel.StateKey("dns", name.value).some

  override def dryRunInstall: Vector[String] =
    Vector(s"dns record ${self.compactJson}")

  override def isActionNeeded: M[Boolean] =
    zservice[DnsService]
      .flatMap(_.isActionNeeded(this))

  /**
   * applies the state for just this system state and no sub states
   */
  override def runApplyNewState: M[Unit] =
    zservice[DnsService]
      .flatMap(_.upsert(this))

  /**
   * uninstalls the state for just this system state and no sub states
   */
  override def runUninstallObsolete(interpreter: Interpreter): M[Unit] =
    zservice[DnsService]
      .flatMap(_.delete(this))

}
