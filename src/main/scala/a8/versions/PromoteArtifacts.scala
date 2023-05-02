package a8.versions

import a8.versions.RepositoryOps.RepoConfigPrefix
import io.accur8.neodeploy.model.{Artifact, Organization}
import a8.versions.model.{ArtifactResponse, ResolutionRequest}
import a8.shared.SharedImports.*
import a8.shared.app.BootstrapConfig.{TempDir, WorkDir}
import zio.Scope
import a8.shared.ZFileSystem
import PromoteArtifacts.*
import io.accur8.neodeploy.PredefAssist.{loggerF, *}
import io.accur8.neodeploy

object PromoteArtifacts {

  object Dependencies {
    given CanEqual[Dependencies,Dependencies] = CanEqual.derived
  }
  enum Dependencies(val name: String):
    case Nothing extends Dependencies("nothing")
    case Validate extends Dependencies("validate")
    case Promote extends Dependencies("promote")

  object ClassifierExtension {
    given CanEqual[Dependencies,Dependencies] = CanEqual.derived
  }
  enum ClassifierExtension(val suffix: String, val mavenPropertyName: String, val required: Boolean) derives CanEqual:
    case Pom extends ClassifierExtension(".pom", "pomFile", true)
    case Jar extends ClassifierExtension(".jar", "file", true)
    case SourceJar extends ClassifierExtension("-sources.jar", "sources", false)
    case JavadocJar extends ClassifierExtension("-javadoc.jar", "javadoc", false)

  case class ClassifiedArtifact(
    artifactResponse: ArtifactResponse,
    classifierExtension: ClassifierExtension,
  ) {
    lazy val filename = {
      import artifactResponse._
      z"${module}-${version}${classifierExtension.suffix}"
    }
    lazy val m2RepoPath = {
      import artifactResponse._
      z"${organizationAsPath}/${module}/${version}/${this.filename}"
    }

  }

  type M[A] = zio.ZIO[zio.Scope & TempDir,Throwable,A]

  lazy val IoDotAccur8Organization = neodeploy.model.Organization("io.accur8")

}

case class PromoteArtifacts(
  resolutionRequest: ResolutionRequest,
  dependencies: Dependencies,
) {

  lazy val mavenRepo = RepositoryOps(RepoConfigPrefix.maven)
  lazy val locusRepo = RepositoryOps(RepoConfigPrefix.locus)

  lazy val stagingUri = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"

  lazy val Organization = resolutionRequest.organization
  lazy val Artifact = resolutionRequest.artifact

  def runT: M[Unit] = {
    val resolutionResponse = RepositoryOps.runResolve(resolutionRequest)
    val atv = artifactsToValidate(resolutionResponse.artifacts)
    val atp = artifactsToPromote(resolutionResponse.artifacts)

    for {
      _ <- loggerF.info(s"running with ${resolutionRequest} ${dependencies}")
      _ <- loggerF.info(s"artifacts to promote ${atp.map(_.filename).mkString(" ")} --  artifacts to validate ${atv.map(_.filename).mkString(" ")}")
      _ <-
        atv
          .map(validate)
          .sequencePar
          .as(())
      _ <-
        atp
          .map(promote)
          .sequencePar
          .flatMap(runActualPromotionsEffects =>
            // we have to run the maven commands serially otherwise we can end up with multiple staging repos
            runActualPromotionsEffects
              .flatten
              .sequence
          )
      _ <- loggerF.info("""
promotion completed successfully log into https://s01.oss.sonatype.org to manually release the promoted items to staging
credentials are in the place you would expect them to be
        """)
    } yield ()
  }

  def artifactsToValidate(artifacts: Iterable[ArtifactResponse]): Iterable[ArtifactResponse] = {
    val atp = artifactsToPromote(artifacts).toSet
    artifacts
      .collect {
        case ar@ArtifactResponse(_, Organization, _, _, _) if dependencies == Dependencies.Validate && !atp.contains(ar)=>
          ar
      }
  }

  def artifactsToPromote(artifacts: Iterable[ArtifactResponse]): Iterable[ArtifactResponse] =
    artifacts
      .collect {
        case ar@ ArtifactResponse(_, Organization, _, _, _) if dependencies == Dependencies.Promote =>
          ar
        case ar@ ArtifactResponse(_, Organization, Artifact, _, _) =>
          ar
      }

  def validate(artifact: ArtifactResponse): M[Unit] = {
    val ca = ClassifiedArtifact(artifact, ClassifierExtension.Jar)
    for {
      mavenExists <- mavenRepo.exists(ca)
      _ <-
        if ( mavenExists ) {
          loggerF.info(s"${artifact.filename} is validated and in maven")
        } else {
          zfail(new RuntimeException(s"${artifact.filename} is not in maven"))
        }
    } yield ()
  }

  def needsPromotion(artifact: ArtifactResponse): M[Boolean] = {
    val ca = ClassifiedArtifact(artifact, ClassifierExtension.Jar)
    for {
      mavenExists <- mavenRepo.exists(ca)
      locusExists <- locusRepo.exists(ca)
    } yield locusExists && !mavenExists
  }

  def promote(artifact: ArtifactResponse): M[Option[M[Unit]]] = {
    needsPromotion(artifact).flatMap {
      case false =>
        loggerF.info(s"${artifact.filename} does not need promotion it is already in maven")
          .as(None)
      case true =>
        for {
          _ <- loggerF.info(s"promoting ${artifact.filename} and it's sources and javadoc jars")
          workDir <- workDirectoryZ
          preparedArtifacts <-
            ClassifierExtension
              .values
              .toSeq
              .map(ce => prepareForPromotion(ClassifiedArtifact(artifact, ce), workDir))
              .sequencePar
              .map(_.flatten)
        } yield Some(runPromote(preparedArtifacts, workDir))
    }
  }

  def prepareForPromotion(classifiedArtifact: ClassifiedArtifact, workDir: ZFileSystem.Directory): M[Option[ClassifiedArtifact]] = {
    // check for the artifact in maven
    // check for the artifact in locus
    // if artifact is in locus and not maven push it to maven

    for {
      _ <- loggerF.debug(s"promote(${classifiedArtifact.filename})")
      mavenExists <- mavenRepo.exists(classifiedArtifact)
      locusExists <- locusRepo.exists(classifiedArtifact)
      result <-
        {
          if ( classifiedArtifact.classifierExtension == ClassifierExtension.Pom || locusExists && !mavenExists ) {
            download(classifiedArtifact, workDir)
              .as(Some(classifiedArtifact))
          } else if ( locusExists && mavenExists ) {
            zsucceed(None)
          } else {
            zfail(new RuntimeException(s"unexpected state for ${classifiedArtifact.filename} locusExists = ${locusExists} mavenExists = ${mavenExists}"))
          }
        }
    } yield result
  }

  def download(classifiedArtifact: ClassifiedArtifact, workDir: ZFileSystem.Directory): M[Unit] = {
    val file = workDir.file(classifiedArtifact.filename)
    for {
      _ <- loggerF.info(s"downloading ${classifiedArtifact.filename}")
      _ <- locusRepo.download(classifiedArtifact, file)
    } yield ()
  }

  def runPromote(preparedArtifacts: Seq[ClassifiedArtifact], workDir: ZFileSystem.Directory): M[Unit] = {

    val artifactResponse = preparedArtifacts.head.artifactResponse

    val mavenProps =
      preparedArtifacts
        .map(pa => s"-D${pa.classifierExtension.mavenPropertyName}=${pa.filename}")

    val args =
      Seq(
        "mvn",
        //          "--settings=settings.xml",
        "org.apache.maven.plugins:maven-gpg-plugin:1.3:sign-and-deploy-file",
      ) ++ mavenProps
        ++
        Seq(
          "-Pgpg",
          s"-Durl=${stagingUri}",
          "-DrepositoryId=ossrh",
        )

    val exec = Exec(args, a8.shared.FileSystem.dir(workDir.absolutePath).some)

    val runMavenEffect = zsuspend {
      val result = exec.execCaptureOutput(false)
      if (result.exitCode == 0) {
        zunit
      } else {
        zfail(new RuntimeException(s"exit code ${result.exitCode} --(stdout)\n${result.stdout}\n--(stderr)\n${result.stderr}"))
      }
    }

    for {
      _ <-
        for {
          _ <- loggerF.info(s"promoting ${artifactResponse.filename} via ${exec}")
          _ <- runMavenEffect
        } yield ()
    } yield ()

  }

}
