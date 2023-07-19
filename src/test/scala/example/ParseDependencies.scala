package example

import a8.versions.SbtDependencyParser

object ParseDependencies extends App {

  val deps1 =
    """
"net.codingwell" %% "scala-guice" % "5.1.0" exclude("com.google.code.findbugs", "jsr305")
    """

  val deps2 = """
        "com.github.ghik" %% "silencer-lib" % myUnresolvedVar % "compile",
        "com.github.ghik" %% "silencer-lib" % myvar % "compile",
        "com.github.ghik" %% "silencer-lib" % "0.5" % "compile",
    """

  val deps = deps1

  val parms =
    Map(
      "log4jVersion" -> "2.9.1",
      "scalaVersion" -> "2.12.5"
    )

  SbtDependencyParser.parse(deps)
    .foreach(println)

}
