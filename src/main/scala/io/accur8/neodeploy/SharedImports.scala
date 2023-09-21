package io.accur8.neodeploy


import io.accur8.neodeploy.Layers
import io.accur8.neodeploy.systemstate.SystemStateModel

object SharedImports extends a8.shared.SharedImports {

  type N[A] = Layers.N[A]
  type M[A] = SystemStateModel.M[A]

  val VFileSystem = io.accur8.neodeploy.VFileSystem

}
