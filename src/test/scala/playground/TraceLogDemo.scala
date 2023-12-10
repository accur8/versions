package playground


import a8.shared.app.BootstrappedIOApp
import a8.shared.app.BootstrappedIOApp.BootstrapEnv
import io.accur8.neodeploy.SharedImports.*
import playground.TraceLogDemo.foo
import zio.ZIO

object TraceLogDemo extends BootstrappedIOApp {


  def foo2(x: Int): String = {
//    val text = implicitly[sourcecode.Text[Int]]
    val args = implicitly[sourcecode.Args]
    traceMethod(x.toString) {
//      println(text)
      println(args)
      x.toString
    }
  }

  override def runT: ZIO[BootstrapEnv, Throwable, Unit] =
    zblock(
//      println(foo2(1))
      foo("baz", 42)(true) // foo(bar=baz, baz=42)(p=true)
    )

  def debug(implicit name: sourcecode.Name, args: sourcecode.Args): Unit = {
    println(name.value + args.value.map(_.map(a => a.source + "=" + a.value).mkString("(", ", ", ")")).mkString(""))
  }

  def foo(bar: String, baz: Int)(p: Boolean): Unit = {
    debug
  }

}
