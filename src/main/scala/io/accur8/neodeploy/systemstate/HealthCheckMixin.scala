package io.accur8.neodeploy.systemstate


import a8.shared.SharedImports._
import io.accur8.neodeploy.HealthchecksDotIo
import io.accur8.neodeploy.systemstate.SystemStateModel._

trait HealthCheckMixin extends SystemStateMixin { self: SystemState.HealthCheck =>

  override def dryRunInstall: Vector[String] = Vector(s"upsert and enable healthcheck ${data.name}")
  override def dryRunUninstall(interpreter: Interpreter): Vector[String] = Vector(s"disable healthcheck ${data.name}")

  def stateKey = StateKey("healthcheck", data.name).some

  def isActionNeeded: M[Boolean] =
    for {
      service <- zservice[HealthchecksDotIo]
      b <- service.isUpdateNeeded(data)
    } yield b

  override def runApplyNewState: M[Unit] =
    for {
      service <- zservice[HealthchecksDotIo]
      b <- service.upsert(data)
    } yield ()

  override def runUninstallObsolete(interpreter: Interpreter): M[Unit] =
    for {
      service <- zservice[HealthchecksDotIo]
      b <- service.disable(data)
    } yield ()

}
