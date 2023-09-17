package a8.appinstaller


import java.nio.file.{Files, Path, Paths}
import java.util
import a8.appinstaller.AppInstallerConfig.LibDirKind
import a8.shared.FileSystem.{Directory, File}
import a8.common.logging.Logging
import a8.versions.Build.BuildType
import a8.versions.RepositoryOps.DependencyTree
import a8.versions.{RepositoryOps, ParsedVersion, ast}
import a8.versions.ast.Dependency
import predef._
import a8.shared.SharedImports._
import io.accur8.neodeploy.PredefAssist._

case class InstallBuilder(
  unresolvedConfig: AppInstallerConfig,
  repositoryOps: RepositoryOps,
) extends Logging {

  lazy val unresolvedArtifact = unresolvedConfig.unresolvedArtifact

  lazy val appDir = resolvedConfig.resolvedInstallDir

  lazy val resolvedConfig =
    unresolvedConfig.copy(version = rootVersion)

  lazy val libDir: Directory = appDir \\ "lib"

  def build(): Unit = {
    buildLibDir()
    val jarFileName = z"${resolvedConfig.artifact}-${resolvedConfig.version.toString()}.jar"
    val jarFile =
      dependencyResult
        .localArtifacts
        .find(f => f.getName == jarFileName)
        .getOrElse(sys.error(s"unable to find ${jarFileName} in dependency tree"))
//    JarMetadata.process(appDir, jarFile)
  }

  lazy val dependencyResult: DependencyTree = {
    val result =
      repositoryOps
        .resolveDependencyTree(unresolvedArtifact.asCoursierModule, rootVersion)(BuildType.ArtifactoryBuild)
    result
  }

  private def buildLibDir() = {
    libDir.makeDirectories()
    libDir.entries().foreach {
      case f: File =>
        f.delete()
      case d: Directory =>
        d.deleteChildren()
        d.delete()
    }

    for (fromFile <- dependencyResult.localArtifacts) {
      val toFile: File = libDir \ fromFile.getName
      resolvedConfig.resolvedLibDirKind match {
        case LibDirKind.Symlink =>
          logger.debug("symlinking artifact " + fromFile)
          val link: Path = Paths.get(toFile.getCanonicalPath)
          val target: Path = Paths.get(fromFile.getCanonicalPath)
          Files.createSymbolicLink(link, target)
        case LibDirKind.Copy =>
          logger.trace("copying artifact " + fromFile)
          Files.copy(Paths.get(fromFile.getCanonicalPath), Paths.get(toFile.getCanonicalPath))
        case LibDirKind.Repo =>
          // noop
      }
    }
  }

  lazy val inventory: InstallInventory =
    InstallInventory(
      resolvedConfig,
      classpath = classpath
    )

  lazy val classpath: Seq[String] =
    dependencyResult
      .localArtifacts
      .map { file =>
        file.getCanonicalPath
      }

  lazy val rootVersion: ParsedVersion = {
    if ( unresolvedArtifact.version.rawValue == "latest") {
      val versions =
        repositoryOps
          .remoteVersions(unresolvedArtifact.asCoursierModule)
          .filter(v => unresolvedConfig.branch.isEmpty || unresolvedConfig.branch == v.buildInfo.map(_.branch))
      versions.toList.sorted.last
    } else {
      ParsedVersion.parse(unresolvedArtifact.version.rawValue).get
    }
  }

}
