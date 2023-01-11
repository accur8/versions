package a8.versions


import a8.shared.{Chord, CompanionGen, StringValue}
import a8.shared.FileSystem.Directory
import a8.shared.HoconOps.parseHocon
import a8.shared.SharedImports._
import a8.shared.HoconOps._
import a8.versions.Mxmodel._
import a8.versions.RepositoryOps.RepoConfigPrefix
import io.accur8.neodeploy.CodeBits
import io.accur8.neodeploy.model.{AuthorizedKey, Personnel}
import io.accur8.neodeploy.resolvedmodel.ResolvedRepository
import zio.Task

object model {

  object BranchName extends StringValue.Companion[BranchName] {
  }
  case class BranchName(value: String) extends StringValue {
    override def toString: String = value
  }


  object impl {
    /** quote string */
    def q(s: String): Chord = {
      Chord.str('"'.toString + s + '"')
    }
  }

  object CompositeBuild {
    def apply(codeRoot: Directory): CompositeBuild = {

      val f = codeRoot \ "modules.conf"
      if ( f.exists() ) {
        CompositeBuild(List(f.parent -> None))

      } else {

//        println(codeRoot)
//        println(codeRoot.entries.toList)

        val moduleConfs =
          codeRoot
            .subdirs()
            .map(_ \ "modules.conf")
//            .map{ p => println(p) ; p }
            .filter(_.exists())

        if ( moduleConfs.isEmpty ) {
          sys.error("no modules.conf found")
        }

        val repos =
          moduleConfs
            .map { f =>
              f.parent -> Some(RepoPrefix(f.parent.name, f.parent.name))
            }
        CompositeBuild(repos)
      }
    }
  }

  case class CompositeBuild(
    repos: Iterable[(Directory, Option[RepoPrefix])]
  ) {

    lazy val resolvedRepos =
      repos.map(t => ResolvedRepo(this, t._1, t._2))

    lazy val resolvedModules: Iterable[ResolvedModule] =
      for (
        repo <- resolvedRepos ;
        module <- repo.astRepo.modules
      ) yield ResolvedModule(repo, module)

  }

  case class ResolvedRepo(
    compositeBuild: CompositeBuild,
    repoRootDir: Directory,
    prefix: Option[RepoPrefix],
  ) {

    def findDependencyViaSbtName(sbtName: String) = {
      compositeBuild
        .resolvedModules
        .find(_.sbtName == sbtName)
        .getOrError(s"unable to find dependsOn for ${sbtName}")
    }


    lazy val astRepo = {
      val file = repoRootDir.file("modules.conf")
      try {
        parseHocon(file.readAsString())
          .read[ast.Repo]
      } catch {
        case e: Exception =>
          println(s"error processing ${file}")
          throw e
      }
    }

    lazy val versionDotPropsMap: Map[String, String] =
      repoRootDir
        .file("version.properties")
        .readAsString()
        .linesIterator
        .map(_.trim)
        .filterNot(l => l.length == 0 || l.startsWith("#"))
        .flatMap {
          _.splitList("=", limit = 2, dropEmpty = false) match {
            case List(l,r) => Some(l -> r)
            case _ => None
          }
        }
        .toMap

    lazy val variables: Iterable[(String, String)] =
      versionDotPropsMap
        .filterNot { case (k,v) =>
          val kl = k.toLowerCase
          kl.endsWith(".upgrade") || kl =:= "this"
        }

  }

  case class RepoPrefix(
    dir: String,
    prefix: String
  )

  case class ResolvedModule(
    repo: ResolvedRepo,
    astModule: ast.Module
  ) {

    def includeInGradle: Boolean =
      astModule.projectType match {
        case None | Some("cross") =>
          true
        case Some("js") | Some("haxe") | Some("sass") =>
          false
        case _ =>
          false
      }

    lazy val organization = repo.astRepo.organization

    lazy val dependentModulesInComposite: Set[ResolvedModule] =
      repo
        .compositeBuild
        .resolvedModules
        .filter { m =>
          dependenciesByOrgArtifact.contains(m.organization -> m.resolveArtifactName)
        }
        .toSet

    lazy val dependenciesByOrgArtifact: Map[(String,String),ast.Dependency] =
      rawResolvedDependencies
        .map(d => (d.organization -> d.artifactName) -> d)
        .toMap

    lazy val prefix =
      repo.prefix.map(_.prefix + "_").getOrElse("")

    def prefixName(name: String): String =
      prefix + name

    lazy val gradleName = {
      val s = resolveDirectory.splitList("/")
      if ( s.length > 1 ) {
        s.dropRight(1).mkString("/") + ":" + s.last
      } else {
        resolveDirectory
      }
    }

    lazy val dependsOnInComposite =
      dependentModulesInComposite
        .map { rm =>
          (rm.resolveProjectType, resolveProjectType) match {
            case ("cross", "jvm") =>
              rm.sbtName + "JVM"
            case ("cross", "js") =>
              rm.sbtName + "JS"
            case _ =>
              rm.sbtName
          }
        }

    lazy val dependsOn =
      astModule
        .dependsOn
        .map(prefixName) ++ dependsOnInComposite

    lazy val sbtName = prefixName(astModule.sbtName)

    lazy val aggregateModules =
      subModuleNames.getOrElse(List(sbtName))

    lazy val subModuleNames =
      submodules.map(_.map(_._1))

    lazy val subModuleLines =
      submodules.toList.flatMap(_.map(sm => "lazy val " + sm._1 + " = " + sm._2))

    lazy val submodules =
      if ( resolveProjectType == "cross" ) Some(List("jvm", "js").map(s => (sbtName + s.toUpperCase, sbtName + "." + s.toLowerCase)))
      else None

    lazy val resolveProjectType = astModule.projectType.getOrElse("jvm")
    lazy val resolveArtifactName = astModule.artifactName.getOrElse(astModule.sbtName)
    lazy val resolveDirectory: String = repo.prefix.map(_.dir + "/").getOrElse("") + astModule.directory.getOrElse(astModule.sbtName)
    lazy val dependencies: Iterable[ast.Dependency] =
      rawResolvedDependencies
        .filterNot { d =>
          dependentModulesInComposite.exists(m => m.organization == d.organization && m.resolveArtifactName == d.artifactName)
        }

    lazy val rawResolvedDependencies: Iterable[ast.Dependency] = {
      astModule
        .dependencies
        .toList
        .flatMap(d => SbtDependencyParser.parse(d))
    }
    lazy val resolveJvmDependencies: Iterable[ast.Dependency] = {
      astModule
        .jvmDependencies
        .toList
        .flatMap(d => SbtDependencyParser.parse(d))
    }
    lazy val resolveJsDependencies: Iterable[ast.Dependency] = {
      astModule
        .jsDependencies
        .toList
        .flatMap(d => SbtDependencyParser.parse(d))
    }


    lazy val allDependencyLines = {
      val versionDotPropsMap = repo.versionDotPropsMap
      (
        dependencyLines("settings", dependencies, versionDotPropsMap) ++
          dependencyLines("jvmSettings", resolveJvmDependencies, versionDotPropsMap) ++
          dependencyLines("jsSettings", resolveJsDependencies, versionDotPropsMap)
      )
    }

    lazy val extraSettingsLines = {
      astModule.extraSettings match {
        case None =>
          Nil
        case Some(es) =>
          val lines = es.linesIterator.filter(_.trim.length > 0).toList
          val indent = lines.head.indexOf(lines.head.trim)
          val unindentedLines =
            lines.map(_.zipWithIndex.dropWhile(t => t._1.isWhitespace && t._2 < indent).map(_._1).mkString)
          unindentedLines
      }
    }

    lazy val settingsLines = {
      val dependsOnLines = dependsOn.map(d => s".dependsOn(${d})")
      val dependenciesLines = allDependencyLines
      dependsOnLines ++ dependenciesLines ++ extraSettingsLines
    }


    def dependencyLines(settingName: String, deps: Iterable[ast.Dependency], versionDotPropsMap: Map[String,String]): Iterable[String] = {
      if ( deps.nonEmpty ) {
        val header = List(
          s".${settingName}(",
          "  libraryDependencies ++= Seq("
        )

        val dependenciesLines =
          deps
            .map(_.asSbt(versionDotPropsMap))
            .map(d => "    " + d.toString.trim + ",")

        val trailer = List("  )", ")")

        header ++ dependenciesLines ++ trailer

      } else {
        Nil
      }
    }

  }


  object ArtifactResponse extends MxArtifactResponse
  @CompanionGen
  case class ArtifactResponse(
    url: String,
    organization: String,
    module: String,
    version: String,
    extension: String,
  ) {

    lazy val filename =
      module + "-" + version + "." + extension

    lazy val m2RepoPath =
      organization.replace('.', '/') + "/" + module + "/" + version + "/" + module + "-" + version + "." + extension

  }

  object ResolutionResponse extends MxResolutionResponse
  @CompanionGen
  case class ResolutionResponse(
    request: ResolutionRequest,
    version: String,
    artifacts: Iterable[ArtifactResponse],
  )

  object ResolutionRequest extends MxResolutionRequest
  @CompanionGen
  case class ResolutionRequest(
    repoPrefix: RepoConfigPrefix = RepoConfigPrefix.default,
    organization: String,
    artifact: String,
    version: String,
    branch: Option[BranchName],
  ) {
    lazy val coursierModule =
      coursier.Module(coursier.Organization(organization), coursier.ModuleName(artifact))
  }

}
