package a8.versions.apps

import a8.common.HoconOps._
import a8.versions.model.Repo
import a8.common.CommonOps._

object GenerateBuildDotSbt extends App {
  val g = new GenerateBuildDotSbt(m3.fs.dir("/Users/glen/code/manna/"))
//  val g = new GenerateBuildDotSbt(m3.fs.dir("/Users/glen/code/model3/"))
  println(g.content)
}


class GenerateBuildDotSbt(repoDir: m3.fs.Directory) {

  import a8.versions.model.impl.q
  import m3.Chord
  import m3.Chord._

/*
  generate the following files

  build.sbt
  project/build.properties
  project/plugins.sbt

*/


  lazy val repo =
    parseHocon(repoDir.file("modules.conf").readText)
      .read[Repo]

  lazy val versionDotPropsMap =
    repoDir
      .file("version.properties")
      .readText
      .lines
      .map(_.trim)
      .filterNot(l => l.length == 0 || l.startsWith("#"))
      .flatMap {
        _.splitList("=", limit = 2, dropEmpty = false) match {
          case List(l,r) => Some(l -> r)
          case _ => None
        }
      }
      .toMap

  lazy val scalaVersion = versionDotPropsMap("scalaVersion")

  lazy val content = s"""

${
    repo.header.getOrElse("import a8.sbt_a8.{SharedSettings => Common}")
}

scalacOptions in Global ++= Seq("-deprecation", "-unchecked", "-feature")

resolvers in Global += "a8-repo" at "https://accur8.artifactoryonline.com/accur8/all/"

publishTo in Global := Some("a8-repo-publish" at "https://accur8.artifactoryonline.com/accur8/libs-releases-local/")

credentials in Global += Credentials(Path.userHome / ".sbt" / "credentials")

scalaVersion in Global := "${scalaVersion}"

organization in Global := "${repo.organization}"

version in Global := a8.sbt_a8.versionStamp(file("."))


${
  repo.modules.map { module =>
s"""
lazy val ${module.sbtName} =
  Common
    .${module.resolveProjectType}Project("${module.resolveArtifactName}", file("${module.resolveDirectory}"))
${

  val dependsOnLines = module.dependsOn.map(d => s".dependsOn(${d})")
  val dependenciesLines = module.allDependencyLines(versionDotPropsMap)

  val allLines = dependsOnLines ++ dependenciesLines

  allLines.map("    " + _).mkString("\n")

}"""}
  .mkString("\n\n")
}${
  repo.modules.flatMap(_.subModuleLines) match {
    case l if l.isEmpty => ""
    case l =>
      l.mkString("\n\n","\n","\n")
  }
}

lazy val root =
  Common.jvmProject("root", file("."), id = Some("root"))
    .settings( publish := {} )
    .aggregate(${repo.modules.flatMap(_.aggregateModules).mkString(", ")})


   """

}
