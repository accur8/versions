package a8.versions

import a8.versions.Build.BuildType
import coursier.Resolution
import coursier.core.{Dependency, Module}


sealed trait Upgrade {

  def resolveVersion(upgrades: Map[String, Upgrade])(implicit buildType: BuildType): Version
  def resolveDependencyTree(upgrades: Map[String, Upgrade])(implicit buildType: BuildType): Resolution

}

object Upgrade {

  case class LatestArtifact(
    module: Module,
    branch: String,
  ) extends Upgrade {

    lazy val remoteVersions =
      RepositoryOps
        .remoteVersions(module)
        .filter(_.buildInfo.exists(_.branch == branch))
        .toIndexedSeq

    lazy val localVersions =
      RepositoryOps
        .localVersions(module)
        .filter(_.buildInfo.exists(_.branch == branch))
        .toIndexedSeq

    def resolveVersion(upgrades: Map[String, Upgrade])(implicit buildType: BuildType): Version = {
      val versions =
        if (buildType.useLocalRepo) localVersions ++ remoteVersions
        else remoteVersions

      versions
        .sorted(Version.orderingByMajorMinorPathBuildTimestamp)
        .last

    }

    def resolveDependencyTree(upgrades: Map[String, Upgrade])(implicit buildType: BuildType): Resolution = {
      val resolvedVersion = resolveVersion(upgrades)
      RepositoryOps.resolveDependencyTree(module, resolvedVersion)
    }

  }

  case class ArtifactInDependencyTree(
    dependencyTreeProp: String,
    module: Module,
  ) extends Upgrade {


    override def resolveVersion(upgrades: Map[String, Upgrade])(implicit buildType: BuildType): Version = {
      val upgrade =
        upgrades
          .get(dependencyTreeProp)
          .getOrElse(throw new RuntimeException(s"unable to resolve dependencyTreeProp ${dependencyTreeProp}"))
      val dependencies =
        upgrade
          .resolveDependencyTree(upgrades)
          .finalDependenciesCache
          .keys
      val dep =
        dependencies
          .find(d => d.module.organization == module.organization && d.module.name == module.name)
          .getOrElse(sys.error(s"unable to resolve ${module.organization}:${module.name} in ${dependencyTreeProp}"))
      Version.parse(dep.version).get
    }

    def resolveDependencyTree(upgrades: Map[String, Upgrade])(implicit buildType: BuildType): Resolution = {
      val resolvedVersion = resolveVersion(upgrades)
      RepositoryOps.resolveDependencyTree(module, resolvedVersion)
    }

  }

  def parse(v: String): Upgrade =
    try {
      v.split("->").toList match {
        case List(sourceProp, target) =>
          target.split(":").toList match {
            case List(org, artifact) =>
              ArtifactInDependencyTree(
                dependencyTreeProp = sourceProp.trim,
                module = Module(org.trim, artifact.trim, Map())
              )
          }
        case List(artifact) =>
          artifact.split(":").toList match {
            case List(org, artifact, branch) =>
              LatestArtifact(Module(org.trim, artifact.trim, Map()), branch.trim)
          }
      }
    } catch {
      case e: Exception =>
        throw new RuntimeException(s"unable to parse upgrade --> ${v}")
    }


}