package a8.versions.apps

import java.util.Date
import a8.versions.{BuildTimestamp, RepositoryOps}

object AhsVersionDance {

  import a8.versions.Build._

  def main(args: Array[String]): Unit = {

    implicit val buildType = BuildType.ArtifactoryBuild
//      implicit val buildType = BuildType.LocalBuild

    val start = new Date()

    val repositoryOps = RepositoryOps.default

    implicit val buildTimestamp = Some(BuildTimestamp.now())

//    publish(codeHome \\ "model3")
//    upgradeAndPublish(codeHome \\ "manna", repositoryOps)
//    upgradeAndPublish(codeHome \\ "qubes", repositoryOps)
//    upgrade(codeHome \\ "ahs" \\ "scala", repositoryOps)


    println(s"started at ${start}")
    println(s"started at ${new Date}")
    println(s"${(System.currentTimeMillis - start.getTime)/1000} seconds processing time")
  }

}
