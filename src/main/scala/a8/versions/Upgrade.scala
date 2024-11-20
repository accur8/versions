package a8.versions


import a8.versions.Build.BuildType
import a8.versions.model.BranchName
import coursier.Resolution
import coursier.core.{Dependency, Module, ModuleName, Organization}
import io.accur8.neodeploy.PredefAssist.{given, _}

sealed trait Upgrade {

  def resolveVersion(upgrades: Map[String, Upgrade], repositoryOps: RepositoryOps)(implicit buildType: BuildType): ParsedVersion
  def resolveDependencyTree(upgrades: Map[String, Upgrade], repositoryOps: RepositoryOps)(implicit buildType: BuildType): Resolution

}

object Upgrade {

  case class LatestArtifact(
    module: Module,
    branch: BranchName,
  ) extends Upgrade {

    def remoteVersions(repositoryOps: RepositoryOps): IndexedSeq[ParsedVersion] =
      repositoryOps
        .remoteVersions(module)
        .filter(_.buildInfo.exists(_.branch == branch))
        .toIndexedSeq

    def localVersions(repositoryOps: RepositoryOps) =
      repositoryOps
        .localVersions(module)
        .filter(_.buildInfo.exists(_.branch == branch))
        .toIndexedSeq


    override def resolveVersion(upgrades: Map[String, Upgrade], repositoryOps: RepositoryOps)(implicit buildType: BuildType): ParsedVersion = {
      val versions =
        if (buildType.useLocalRepo) localVersions(repositoryOps) ++ remoteVersions(repositoryOps)
        else remoteVersions(repositoryOps)

      val versionOpt =
        versions
          .sorted(ParsedVersion.orderingByMajorMinorPathBuildTimestamp)
          .lastOption

      versionOpt match {
        case Some(version) =>
          version
        case None =>
          sys.error(s"unable to find version for ${this}")
      }

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

    override def resolveVersion(upgrades: Map[String, Upgrade], repositoryOps: RepositoryOps)(implicit buildType: BuildType): ParsedVersion = {
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
      ParsedVersion.parse(dep.version).get
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
            case List(org, artifact, branchStr) =>
              LatestArtifact(Module(Organization(org.trim), ModuleName(artifact.trim), Map()), BranchName(branchStr.trim))
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
