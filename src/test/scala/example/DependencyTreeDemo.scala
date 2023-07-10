package example

import a8.versions.Build.BuildType
import a8.versions.{ParsedVersion, RepositoryOps}
import coursier.cache.ArtifactError
import coursier.core.{ModuleName, Organization}

import java.io.File

object DependencyTreeDemo {


  def main(args: Array[String]): Unit = {
    implicit val buildType = BuildType.ArtifactoryBuild

    lazy val treeA8 =
      RepositoryOps
        .default
        .resolveDependencyTree(
          coursier.core.Module(Organization("a8"), ModuleName("a8-qubes-dist_2.12"), Map()),
          ParsedVersion.parse("2.7.0-20180418_0536_master").get
        )

    lazy val treeAhs =
      RepositoryOps
        .default
        .resolveDependencyTree(
          coursier.core.Module(Organization("ahs"), ModuleName("ahs-qubes-runner_2.12"), Map()),
          ParsedVersion.parse("2.7.1-20230707_1108_master").get
        )

    val tree = treeAhs

    val searchStr = "scala-compiler"

    val artifacts = tree.resolution.artifacts()

    val urls =
      artifacts
        .map(_.extra)

    println(urls.mkString("\n"))

    val errors = tree.resolution.errors

    val files = tree.localArtifacts.sortBy(_.getCanonicalPath)

    val lefts: Seq[Either[ArtifactError, File]] = tree.rawLocalArtifacts.filter(_.isLeft)
    val rights: Seq[Either[ArtifactError, File]] = tree.rawLocalArtifacts.filter(_.isRight)


    val depSet = tree.resolution.dependencySet
//    val scalaps = files.filter(_.getCanonicalPath.contains(searchStr))

    val f0 = files.filter(_.getName.contains("scala-lib"))
    val r0 = rights.flatMap(_.toOption).filter(_.getName.contains("scala-lib"))

    toString: @scala.annotation.nowarn

  }

}
