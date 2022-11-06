package a8.versions

import a8.versions.Build.BuildType
import coursier.Resolution
import coursier.core.{Dependency, Module, ModuleName, Organization}


sealed trait Upgrade {

  def resolveVersion(upgrades: Map[String, Upgrade], repositoryOps: RepositoryOps)(implicit buildType: BuildType): Version
  def resolveDependencyTree(upgrades: Map[String, Upgrade], repositoryOps: RepositoryOps)(implicit buildType: BuildType): Resolution

}

object Upgrade {

  case class LatestArtifact(
    module: Module,
    branch: String,
  ) extends Upgrade {

    def remoteVersions(repositoryOps: RepositoryOps) =
      repositoryOps
        .remoteVersions(module)
        .filter(_.buildInfo.exists(_.branch == branch))
        .toIndexedSeq

    def localVersions(repositoryOps: RepositoryOps) =
      repositoryOps
        .localVersions(module)
        .filter(_.buildInfo.exists(_.branch == branch))
        .toIndexedSeq


    override def resolveVersion(upgrades: Map[String, Upgrade], repositoryOps: RepositoryOps)(implicit buildType: BuildType): Version = {
      val versions =
        if (buildType.useLocalRepo) localVersions(repositoryOps) ++ remoteVersions(repositoryOps)
        else remoteVersions(repositoryOps)

      versions
        .sorted(Version.orderingByMajorMinorPathBuildTimestamp)
        .last

    }

    def resolveDependencyTree(upgrades: Map[String, Upgrade], repositoryOps: RepositoryOps)(implicit buildType: BuildType): Resolution = {
      val resolvedVersion = resolveVersion(upgrades, repositoryOps)
      repositoryOps.resolveDependencyTree(module, resolvedVersion).resolution
    }

  }

  case class ArtifactInDependencyTree(
    dependencyTreeProp: String,
    module: Module,
  ) extends Upgrade {

    override def resolveVersion(upgrades: Map[String, Upgrade], repositoryOps: RepositoryOps)(implicit buildType: BuildType): Version = {
      val upgrade =
        upgrades
          .get(dependencyTreeProp)
          .getOrElse(throw new RuntimeException(s"unable to resolve dependencyTreeProp ${dependencyTreeProp}"))
      val dependencies =
        upgrade
          .resolveDependencyTree(upgrades, repositoryOps)
          .finalDependenciesCache
          .keys
      val dep =
        dependencies
          .find(d => d.module.organization == module.organization && d.module.name == module.name)
          .getOrElse(sys.error(s"unable to resolve ${module.organization}:${module.name} in ${dependencyTreeProp}"))
      Version.parse(dep.version).get
    }

    def resolveDependencyTree(upgrades: Map[String, Upgrade], repositoryOps: RepositoryOps)(implicit buildType: BuildType): Resolution = {
      val resolvedVersion = resolveVersion(upgrades, repositoryOps)
      repositoryOps.resolveDependencyTree(module, resolvedVersion).resolution
    }

  }

  def parse(v: String): Upgrade = {
    def error() = sys.error(s"error parsing ${v}")
    try {
      v.split("->").toList match {
        case List(sourceProp, target) =>
          target.split(":").toList match {
            case List(org, artifact) =>
              ArtifactInDependencyTree(
                dependencyTreeProp = sourceProp.trim,
                module = Module(Organization(org.trim), ModuleName(artifact.trim), Map())
              )
            case _ =>
              error()
          }
        case List(artifact) =>
          artifact.split(":").toList match {
            case List(org, artifact, branch) =>
              LatestArtifact(Module(Organization(org.trim), ModuleName(artifact.trim), Map()), branch.trim)
            case _ =>
              error()
          }
        case _ =>
          error()
      }
    } catch {
      case e: Exception =>
        throw new RuntimeException(s"unable to parse upgrade --> ${v}")
    }
  }


}
