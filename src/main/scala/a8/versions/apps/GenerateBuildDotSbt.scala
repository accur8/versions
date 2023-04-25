package a8.versions.apps

import a8.shared.FileSystem.Directory
import a8.shared.{CascadingHocon, ConfigMojo, FileSystem}
import a8.versions.BuildDotSbtGenerator
import a8.versions.model.CompositeBuild

import scala.util.Try

object GenerateBuildDotSbt extends App {

  import a8.Scala3Hacks.*

  lazy val homeDir = FileSystem.userHome


  lazy val path =
    Try {
      ConfigMojo.root.GenerateBuildDotSbt.path.as[String]
    }

  lazy val pathi =
    Try {
      ConfigMojo.root.GenerateBuildDotSbt.paths.as[Iterable[String]]
    }

  lazy val paths =
    ( path.toOption ++ pathi.toOption.toSeq.flatten )
      .map ( homeDir.subdir )

//  run("ahs_aggregate", homeDir \\ "code/ahs/aggregate")

//  run(homeDir \\ "code/odin")
//  run(homeDir \\ "code/model3")
//  run(homeDir \\ "code/ahs-scala")
//  run(homeDir \\ "code/accur8/monadic-html")
//  run(homeDir \\ "code/accur8/odin")
//  run(homeDir \\ "code/accur8/hermes")
//  run(homeDir \\ "code/accur8/model3")
//  run(homeDir \\ "code/build-tools/a8-recipe")
//  run(homeDir \\ "code/build-tools/honeybadger")
//  run(homeDir \\ "code/s3-backup")

//  run(homeDir \\ "code/aggregates")

  paths.foreach { p =>
    run(p)
  }

  def run(d: Directory) = {
    val g = new BuildDotSbtGenerator(d)
    println(s"running ${d.canonicalPath}")
    try {
      g.run()
      println(s"run complete for ${d}")
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }

}
