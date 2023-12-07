package playground


import io.accur8.neodeploy.SharedImports._

object UriDemo extends App {

  val uri = unsafeParseUri("jdbc:postgresql://tulip.accur8.net/postgres")

  val urix = uri.withPath(uri.path.take(uri.path.length - 1) ++ Some("bob"))
  println(urix)

}
