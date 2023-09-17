package io.accur8.neodeploy


import a8.shared.StringValue
import a8.common.logging.{Logging, LoggingF}
import io.accur8.neodeploy.InfrastructureSync.Name
import io.accur8.neodeploy.model.UserLogin
import io.accur8.neodeploy.resolvedmodel.{ResolvedRepository, ResolvedUser}
import io.accur8.neodeploy.systemstate.SystemStateModel.PreviousState

object InfrastructureSync {
  object Name extends StringValue.Companion[Name]
  case class Name(value: String) extends StringValue
}

case class InfrastructureSync(resolvedRepository: ResolvedRepository) extends LoggingF {

  val stateDirectory = resolvedRepository.gitRootDirectory.subdir(".state").subdir("infrastructure")

}
