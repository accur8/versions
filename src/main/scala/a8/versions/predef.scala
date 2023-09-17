package a8.versions

import a8.common.logging.Logger
import sttp.client3.*

import scala.collection.convert.{AsJavaExtensions, AsScalaExtensions}

object predef extends predef

trait predef
  extends AsJavaExtensions
  with AsScalaExtensions
{

  type Logging = a8.common.logging.Logging
  type LoggingF = a8.common.logging.LoggingF


  type Closable = { def close(): Unit }
  def using[A <: Closable, B](r: => A)(f: A => B)(implicit logger: Logger): B = {
    val resource = r
    try {
      f(resource)
    } finally {
      forceClose(resource)
    }
  }

  def using[A <: Closable, B](l:List[A])(f: => B)(implicit logger: Logger): B = {
    try {
      f
    } finally {
      l.foreach(r=>forceClose(r))
    }
  }

  def forceClose[A <: Closable](closeMe: A)(implicit logger: Logger) =
    try {
      import scala.reflect.Selectable.reflectiveSelectable
      closeMe.close()
    } catch {
      case th: Throwable => logger.debug(s"swallowing failed close on ${closeMe}", th)
    }

}
