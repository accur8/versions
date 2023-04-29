package io.accur8.neodeploy


import a8.shared.ZFileSystem.{Directory, File, Path}
import a8.shared.json.JsonCodec
import zio.{Task, ZIO}
import a8.shared.SharedImports.*
import a8.shared.StringValue
import a8.shared.app.{LoggerF, LoggingF}
import a8.shared.json.ast.{JsDoc, JsVal}
import a8.versions.Exec
import io.accur8.neodeploy.Sync.SyncName
import PredefAssist.*
import io.accur8.neodeploy.model.ApplicationName
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemStateModel.*

object Sync extends LoggingF {

  object SyncName extends StringValue.Companion[SyncName] {
    given CanEqual[SyncName, SyncName] = CanEqual.derived
  }
  case class SyncName(value: String) extends StringValue

}



/**
  * A is the state for the specific sync
  * B is the input value to determine sync state
  */
abstract class Sync[A] {

  import Sync._

  val name: SyncName

  def systemState(input: A): M[SystemState]

}
