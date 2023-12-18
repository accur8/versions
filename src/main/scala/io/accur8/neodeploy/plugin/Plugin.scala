package io.accur8.neodeploy.plugin

import a8.shared.json.ast.JsVal
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemStateModel.M


trait Plugin[A] {

  def name: String

  def descriptorJson: JsVal

}

