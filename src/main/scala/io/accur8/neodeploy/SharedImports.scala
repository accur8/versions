package io.accur8.neodeploy


import io.accur8.neodeploy.Layers
import io.accur8.neodeploy.systemstate.SystemStateModel

object SharedImports extends a8.shared.SharedImports {

  type N[A] = Layers.N[A]
  type M[A] = SystemStateModel.M[A]

  val VFileSystem = io.accur8.neodeploy.VFileSystem

  def traceMethod[A](args: String)(fn: =>A)(implicit logger: Logger, name: sourcecode.Name): A = {
    val context = s"${name.value}(${args})"
    val start = System.currentTimeMillis()
    val result = fn
    val end = System.currentTimeMillis()
    logger.debug(s"$context result is ${result} took ${end - start}ms ")
    result
  }

  def traceEffect[R,E,A](args: String)(effect: => zio.ZIO[R,E,A])(implicit loggerF: LoggerF, name: sourcecode.Name): zio.ZIO[R,E,A] = {
    val context = s"${name.value}(${args})"
    loggerF.debug(s"start $context") *>
    effect
      .tap( result =>
        loggerF.debug(s"complete $context result is ${result}")
      )
  }

}
