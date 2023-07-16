package a8.versions

import a8.shared.jdbcf.DatabaseConfig.Password
import a8.shared.{CompanionGen, FileSystem, StringValue}

import java.io.{File, FileInputStream, StringReader}
import java.util.{Base64, Properties}
import a8.versions.Build.BuildType
import a8.versions.PromoteArtifacts.{ClassifiedArtifact, ClassifierExtension}
import a8.versions.RepositoryOps.{DependencyTree, RepoConfigPrefix, ivyLocal}
import a8.versions.Upgrade.LatestArtifact
import a8.versions.model.{ArtifactResponse, ResolutionRequest, ResolutionResponse}
import a8.versions.predef.*
import sttp.model.*
import sttp.client3.*
import coursier.cache.{ArtifactError, Cache}
import coursier.core.{Authentication, Module, ResolutionProcess}
import coursier.maven.MavenRepository
import coursier.util.{Artifact, EitherT, Task}
import coursier.{Dependency, LocalRepositories, Profile, Resolution}

import java.nio.charset.StandardCharsets
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps
import io.accur8.neodeploy
import zio.stream.ZStream
import a8.shared.SharedImports.*

object RepositoryOps extends Logging {

  val default = RepositoryOps(RepoConfigPrefix.default)

  val httpClient = SimpleHttpClient()

  object RepoConfigPrefix extends StringValue.Companion[RepoConfigPrefix] {
    def default = RepoConfigPrefix("repo")
    def locus = RepoConfigPrefix("locus")
    def maven = RepoConfigPrefix("maven")
  }

  case class RepoConfigPrefix(
    value: String,
  ) extends StringValue {

    object impl {
      def readRepoPropertyOpt(suffix: String): Option[String] = {
        val propertyName = s"${value}_${suffix}"
        val result =
          RepoAssist.readRepoPropertyOpt(propertyName) match {
            case None =>
              None
            case Some(s) =>
              Some(s)
          }
        logger.debug("reading repo property: " + propertyName + " = " + result)
        result
      }
    }

    lazy val url =
      (value, impl.readRepoPropertyOpt("url")) match {
        case (_, Some(v)) =>
          v
        case ("maven", _) =>
          val v = "https://repo1.maven.org/maven2"
          logger.debug("using default maven repo url: " + v)
          v
        case t@(("repo", _) | ("locus", _)) =>
          val v = "http://locus.accur8.net/repos/all"
          logger.debug(s"using default ${t._1} url: " + v)
          v
        case _ =>
          sys.error(s"minimally must supply a ${value}_url property or specify the repo as maven")
      }

    lazy val userOpt = impl.readRepoPropertyOpt("user")
    lazy val passwordOpt =  impl.readRepoPropertyOpt("password")

    def authentication: Option[Authentication] = {
      for {
        user <- userOpt
        password <- passwordOpt
      } yield {

        val encodedBasicAuth =
          Base64.getEncoder.encodeToString(
            s"$user:$password".getBytes(StandardCharsets.UTF_8)
          )

        Authentication(user, password)
          .withHttpHeaders(Seq("Authorization" -> s"Basic ${encodedBasicAuth}"))

      }
    }
  }

  case class DependencyTree(
    resolution: Resolution,
  ) {

    import java.io.File

    lazy val rawLocalArtifacts: Seq[Either[ArtifactError, File]] =
        resolution.artifacts().map(Cache.default.file(_).run.unsafeRun())

    lazy val localArtifacts: Seq[File] =
      rawLocalArtifacts
        .flatMap(_.toOption)
        .filter{ f =>
          f.getName.endsWith(".jar")
        }
        .distinct

  }

  lazy val userHome = FileSystem.dir(System.getProperty("user.home"))

  lazy val ivyLocal = userHome \\ ".ivy2" \\ "local"

  def runResolve(request: ResolutionRequest): ResolutionResponse = {
    val repositoryOps = RepositoryOps(request.repoPrefix)

    implicit val buildType = BuildType.ArtifactoryBuild

    lazy val resolvedVersion: ParsedVersion =
      request.version.value match {
        case "latest" =>
          LatestArtifact(request.coursierModule, request.branch.getOrElse(sys.error("branch is required for latest")))
            .resolveVersion(Map.empty, repositoryOps)
        case s =>
          ParsedVersion
            .parse(s)
            .getOrElse(sys.error(s"Invalid version: $s"))
      }

    lazy val dependencyTree: RepositoryOps.DependencyTree =
      repositoryOps
        .resolveDependencyTree(request.coursierModule, resolvedVersion)

    lazy val resolution: Resolution = dependencyTree.resolution

    lazy val artifactResponses: Seq[ArtifactResponse] =
      resolution
        .dependencyArtifacts(None)
        .collect {
          case t@ (dep, pub, artifact) if Resolution.defaultTypes(pub.`type`) =>
            t
        }
        .distinctBy(_._3)
        .map { case (dep, pub, artifact) =>
          val classifier =
            dep.publication.classifier.value.trim match {
              case "" =>
                None
              case s =>
                Some(s)
            }
          ArtifactResponse(
            Uri.unsafeParse(artifact.url),
            neodeploy.model.Organization(dep.module.organization.value),
            neodeploy.model.Artifact(dep.module.name.value),
            neodeploy.model.Version(dep.version),
            pub.ext.value,
            classifier,
          )
        }

    val response =
      ResolutionResponse(
        request,
        io.accur8.neodeploy.model.Version(resolvedVersion.toString()),
        repositoryOps.remoteRepositoryUri,
        artifactResponses,
      )

    response

  }

}

case class RepositoryOps(repoConfigPrefix: RepoConfigPrefix) extends Logging {

  def resolveDependencyTree(module: Module, resolvedVersion: ParsedVersion)(implicit buildType: BuildType): DependencyTree = {

    val context = s"resolveDependencyTree ${module} ${resolvedVersion}"
    logger.debug(s"start ${context}")

    // a8-qubes-server_2.12/2.7.0-20180324_1028_master

    val start = Resolution(
       Seq(
         Dependency(
           module, resolvedVersion.toString()
         )
       )
     )

    val repositories =
      if ( buildType.useLocalRepo ) Seq(localRepository, remoteRepository)
      else Seq(remoteRepository)

    val fetch = ResolutionProcess.fetch(repositories, Cache.default.fetch)

    val resolution: Resolution = ResolutionProcess(start).run(fetch).unsafeRun()

    val errors: Seq[((Module, String), Seq[String])] = resolution.errors

    if ( errors.nonEmpty ) {
      throw new RuntimeException(errors.map(_._2.mkString("\n")).mkString("\n"))
    } else {
      logger.debug(s"completed ${context}")
      DependencyTree(resolution)
    }
  }


  lazy val localRepository = LocalRepositories.ivy2Local

  lazy val remoteRepositoryUri: String = repoConfigPrefix.url
//  def remoteRepositoryUser = repoConfig.user
//  def remoteRepositoryPassword = repoConfig.password

  lazy val remoteRepository =
    MavenRepository(
      remoteRepositoryUri.toString,
      authentication = remoteRepositoryAuthentication,
    )

  def remoteRepositoryAuthentication: Option[Authentication] = repoConfigPrefix.authentication

  def localVersions(module: Module): Iterable[ParsedVersion] = {

    val moduleDir = ivyLocal.subdir(module.organization.value).subdir(module.name.value)

    moduleDir
      .subdirs()
      .flatMap { d =>
        ParsedVersion.parse(d.name).toOption
      }

  }

  /**
    * sorted with most recent version first
    */
  def remoteVersions(module: Module): Iterable[ParsedVersion] = {

    def getVersionXml(artifact: Artifact): Future[Either[String,String]] = {
      try {
        val uri =
          Uri(new java.net.URI(artifact.url))
            .userInfo(None)

        val response = {
          val baseReq = quickRequest.get(uri)
          val req =
            remoteRepositoryAuthentication
              .map(auth => baseReq.auth.basic(auth.user, auth.passwordOpt.get))
              .getOrElse(baseReq)
          RepositoryOps
            .httpClient
            .send(req)
        }

        val body = response.body

        logger.debug("================ " + artifact.url + "\n" + body)

        Future.successful(Right(body))
      } catch {
        case e: Throwable =>
          Future.failed(e)
      }
    }

    def fetch(artifact: Artifact): EitherT[Task, String, String] =
      EitherT(Task(_ => getVersionXml(artifact)))

    val versions =
      remoteRepository.versions(module, fetch).run.unsafeRun() match {
        case Right((value, _)) =>
          value.available
        case Left(msg) =>
          sys.error(msg)
      }

    versions
      .flatMap(v => ParsedVersion.parse(v).toOption)
      .toIndexedSeq
      .sorted(ParsedVersion.orderingByMajorMinorPathBuildTimestamp)
      .reverse

  }

  def exists(classifiedArtifact: ClassifiedArtifact): zio.Task[Boolean] = zblock {

    val repoUri =
      remoteRepositoryUri
        .reverse
        .dropWhile(_ == '/')
        .reverse
        .toString

    val uri =
      Uri.unsafeParse(repoUri + "/" + classifiedArtifact.m2RepoPath)
        .userInfo(None)

    val response = {
      val baseReq = quickRequest.head(uri)
      val req =
        remoteRepositoryAuthentication
          .foldLeft(quickRequest.head(uri)) ( (req, auth) =>
            req.auth.basic(auth.user, auth.passwordOpt.get)
          )
      RepositoryOps
        .httpClient
        .send(req)
    }

    response.code.code == 200

  }


  def download(classifiedArtifact: ClassifiedArtifact, file: a8.shared.ZFileSystem.File): zio.Task[Boolean] = zsuspend {

    val uri =
      Uri.unsafeParse(remoteRepositoryUri + "/" + classifiedArtifact.m2RepoPath)
        .userInfo(None)

    val response = {
      val baseReq: RequestT[Empty, Either[String, File], Any] = basicRequest.response(asFile(file.asJioFile))
      val req =
        remoteRepositoryAuthentication
          .foldLeft(baseReq)((req0, auth) =>
            req0.auth.basic(auth.user, auth.passwordOpt.get)
          )
      RepositoryOps
        .httpClient
        .send(req.get(uri))
    }

    response.body match {
      case Left(body) =>
        if ( response.code.code == 404 )
          zsucceed(false)
        else
          zfail(new RuntimeException(s"http status ${response.code} -- ${body}"))
      case Right(_) =>
        zsucceed(true)
    }

  }


}
