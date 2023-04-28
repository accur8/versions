package a8.versions.apps

import java.util.Date
import a8.versions.{BuildTimestamp, RepositoryOps}

object QubesVersionDance {

  import a8.versions.Build._

  def main(args: Array[String]) = {

    implicit val buildType = BuildType.ArtifactoryBuild
    implicit val buildTimestamp = Some(BuildTimestamp.now())

    val repositoryOps = RepositoryOps.default

    println(s"using buildTimestamp = ${buildTimestamp}")

    val start = new Date()

//    publish(codeHome \\ "model3")
//    upgradeAndPublish(codeHome \\ "manna", repositoryOps)
//    upgrade(codeHome \\ "qubes", repositoryOps)

    println(s"started at ${start}")
    println(s"started at ${new Date}")
    println(s"${(System.currentTimeMillis - start.getTime)/1000} seconds processing time")

  }

}
