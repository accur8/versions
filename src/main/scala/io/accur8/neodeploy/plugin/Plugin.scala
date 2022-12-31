package io.accur8.neodeploy.plugin

import a8.shared.json.ast.JsVal
import io.accur8.neodeploy.Sync
import io.accur8.neodeploy.Sync.SyncName
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemStateModel.M


trait Plugin[A] extends Sync[A] {

  val name: SyncName

  def descriptorJson: JsVal

  def systemState(input: A): M[SystemState]

}

