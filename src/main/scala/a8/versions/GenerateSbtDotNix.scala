package a8.versions


import a8.shared.{FileSystem, StringValue}
import a8.shared.app.BootstrappedIOApp
import a8.shared.app.BootstrappedIOApp.BootstrapEnv
import a8.versions.Build.BuildType
import a8.versions.RepositoryOps.RepoConfigPrefix
import a8.versions.Version.BuildInfo
import a8.versions.model.{ArtifactResponse, BranchName, ResolutionRequest}
import coursier.ModuleName
import coursier.core.Module
import io.accur8.neodeploy.HealthchecksDotIo
import zio.{Chunk, LogLevel, Task, ZIO}

import java.io.File
import java.net.URL
import java.net.http.HttpRequest
import java.security.MessageDigest
import java.util.Base64
import scala.util.Try
import a8.shared.SharedImports._
import org.apache.commons.codec.binary.Base32
import zio.stream.ZStream

case class GenerateSbtDotNix(
  resolutionRequest: ResolutionRequest,
)
  extends BootstrappedIOApp
{

  val parallelism = 20

  // hardcoded to use maven for now
  val resolutionResponseZ: Task[model.ResolutionResponse] = ZIO.attemptBlocking(RepositoryOps.runResolve(resolutionRequest))

  override def defaultZioLogLevel: LogLevel = LogLevel.Trace

  def fetchLine(artifact: ArtifactResponse, nixHash: NixHash): String = {
    val attributes =
      Vector(
        "url" -> artifact.url,
        "sha256" -> nixHash.value,
        "organization" -> artifact.organization,
        "module" -> artifact.module,
        "version" -> artifact.version,
        "m2RepoPath" -> artifact.m2RepoPath,
        "filename" -> artifact.filename,
      )
    s"""    (fetcher { ${attributes.map(t => s"""${t._1} = "${t._2}"; """).mkString(" ")} })"""
  }



  case class NixPrefetchResult(
    nixHash: NixHash,
    nixStorePath: String,
  )

  object NixHash extends StringValue.Companion[NixHash]
  case class NixHash(value: String) extends StringValue

  def nixHash(url: String): Task[NixHash] =
    nixHashFromRepo(url)
      .flatMap {
        case scala.util.Success(value) =>
          zsucceed(value)
        case _ =>
          nixPrefetchUrl(url)
            .map(_.nixHash)
      }
      .trace(s"nixHash(${url})")

  def nixHashFromRepo(url: String): Task[Try[NixHash]] =
    ZIO
      .attemptBlocking {

        val sha256Url = url + ".sha256"

        val request =
          java.net.http.HttpRequest
            .newBuilder()
            .uri(java.net.URI.create(sha256Url))
            .GET()
            .build()

        Try {
          val response = HealthchecksDotIo.impl.unsafeSend(request)
          if (response.statusCode() === 200) {
            val sha256HexStr = response.body()
            import sys.process._
            import scala.language.postfixOps
            val results = (s"nix-hash --to-base32 ${sha256HexStr} --type sha256" !!)
            NixHash(results.trim)
          } else {
            sys.error("next")
          }
        }

      }
      .trace(s"nixHashFromRepo(${url})")

  def nixPrefetchUrl(url: String): Task[NixPrefetchResult] =
    ZIO
      .attemptBlocking {
        import sys.process._
        import scala.language.postfixOps
        val results = (s"nix-prefetch-url --print-path ${url}" !!)
        val lines =
          results
            .linesIterator
            .filter(_.trim.length > 0)
            .toVector
        val result =
          NixPrefetchResult(
            NixHash(lines(0)),
            lines(1),
          )
        result
      }
      .trace(s"nixPrefetchUrl(${url})")


  override def runT: ZIO[BootstrapEnv, Throwable, Unit] = {
    for {
      resolutionResponse <- resolutionResponseZ
      artifacts <-
        ZStream
          .fromIterable[ArtifactResponse](resolutionResponse.artifacts)
          .mapZIOParUnordered(parallelism) { artifact =>
            nixHash(artifact.url)
              .map(artifact -> _)
          }
          .runCollect
      _ <- printThem(artifacts)
    } yield ()
  }


  def printThem(artifacts: Chunk[(ArtifactResponse, NixHash)]): Task[Unit] = {
    val content = s"""
fetcher: [
${artifacts.map(t => fetchLine(t._1, t._2)).mkString("\n")}
]
""".trim + "\n"

    ZIO.attemptBlocking {
      FileSystem.file("sbt-deps.nix").write(content)
      println(content)
    }

  }

}
