package playground


import a8.shared.app.BootstrappedIOApp
import a8.shared.app.BootstrappedIOApp.BootstrapEnv
import io.accur8.neodeploy.SharedImports.*
import zio.ZIO

object TraceLogDemo extends BootstrappedIOApp {


  def foo(x: Int): String =
    traceMethod(x.toString) {
      x.toString
    }

  override def runT: ZIO[BootstrapEnv, Throwable, Unit] =
    zblock(
      foo(1)
    )

}
