package a8.versions


import a8.shared.{CompanionGen, FileSystem, StringValue, ZFileSystem}
import a8.shared.app.{BootstrappedIOApp, LoggingF}
import a8.shared.app.BootstrappedIOApp.BootstrapEnv
import a8.versions.Build.BuildType
import a8.versions.RepositoryOps.RepoConfigPrefix
import a8.versions.Version.BuildInfo
import a8.versions.model.{ArtifactResponse, BranchName, ResolutionRequest, ResolutionResponse}
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
import a8.shared.ZFileSystem.{Directory, Symlink}
import a8.versions.GenerateJavaLauncherDotNix.FullInstallResults
import a8.versions.MxGenerateJavaLauncherDotNix._
import org.apache.commons.codec.binary.{Base32, Hex}
import zio.stream.ZStream

object GenerateJavaLauncherDotNix {

  /**
   * correlates to JvmCliLaunchConfig in the a8.launcher.Main.hx
   */
  object Parms extends MxParms

  @CompanionGen
  case class Parms(
    name: String,
    mainClass: String,
    jvmArgs: List[String] = Nil,
    args: List[String] = Nil,
    repo: RepoConfigPrefix = RepoConfigPrefix.default,
    organization: String,
    artifact: String,
    version: Option[String] = None,
    branch: Option[BranchName] = None,
    webappExplode: Option[Boolean] = None,
    javaVersion: Option[String] = None,

    /** this is ignored */
    dependencyDownloader: Option[String] = None,
  ) {

    lazy val coursierModule =
      coursier.Module(coursier.Organization(organization), coursier.ModuleName(artifact))

    lazy val resolutionRequest: ResolutionRequest =
      ResolutionRequest(
        repoPrefix = repo,
        organization = organization,
        artifact = artifact,
        version = version.getOrElse("latest"),
        branch = branch,
      )

  }

  case class FullInstallResults(
    nixPackageInStore: Directory,
    buildOutLink: Symlink,
  )

}


case class GenerateJavaLauncherDotNix(
  parms: GenerateJavaLauncherDotNix.Parms,
  launcherConfigOnly: Boolean,
)
  extends LoggingF
{

  logger.info(s"using args ${parms.args}")

  val parallelism = 20

  lazy val repositoryOps = RepositoryOps(parms.resolutionRequest.repoPrefix)

  // hardcoded to use maven for now
  val resolutionResponseZ: Task[model.ResolutionResponse] =
    ZIO
      .attemptBlocking(RepositoryOps.runResolve(parms.resolutionRequest))
      .map(r => r.copy(artifacts = r.artifacts.toList.distinct))

  lazy val javaLauncherTemplateContent =
    """
#!/bin/bash

exec _out_/bin/_name_j -cp _out_/lib/*:. _args_ "$@"

    """.trim + "\n"

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
    s"""{ ${attributes.map(t => s"""${t._1} = "${t._2}"; """).mkString(" ")} }"""
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

  def repoAuthHeaders: Seq[(String,String)] =
    repositoryOps
      .remoteRepositoryAuthentication
      .toSeq
      .flatMap(_.httpHeaders)

  def nixHashFromRepo(url: String): Task[Try[NixHash]] =
    ZIO
      .attemptBlocking {

        val sha256Url = url + ".sha256"

        val request = {
          val t0 =
            java.net.http.HttpRequest
              .newBuilder()
              .uri(java.net.URI.create(sha256Url))
              .GET()

          val headers = repoAuthHeaders
          headers.toString()

          val t1 =
            repoAuthHeaders
              .foldLeft(t0)( (t, header) =>
                t.header(header._1, header._2)
              )

          t1.build()

        }

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
        val urlWithAuth =
          sttp.model.Uri.parse(url) match {
            case Left(err) =>
              sys.error(err)
            case Right(u0) =>
              repositoryOps
                .remoteRepositoryAuthentication
                .flatMap(auth => auth.passwordOpt.map(p => u0.userInfo(auth.user,p)))
                .getOrElse(u0)
          }
        val results = (s"/nix/var/nix/profiles/default/bin/nix-prefetch-url --print-path ${urlWithAuth}" !!)
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


  def runNixBuild(workDir: Directory): Task[ZFileSystem.Symlink] = {
    val symlinkName = "build"
    ZIO.attemptBlocking(
      Exec(
        Seq("/nix/var/nix/profiles/default/bin/nix-build", "--out-link", symlinkName, "-E", "with import <nixpkgs> {}; (callPackage ./launcher {})"),
        Some(FileSystem.dir(workDir.absolutePath)),
      ).execCaptureOutput()
    ).as(workDir.symlink(symlinkName))
  }

  def runFullInstall(workDir: Directory): Task[FullInstallResults] = {
    val launcherFilesDir = workDir.subdir("launcher")
    for {
      defaultDotNixContent <- javaLauncherContentT
      _ <- launcherFilesDir.file("default.nix").write(defaultDotNixContent)
      _ <- launcherFilesDir.file("java-launcher-template").write(javaLauncherTemplateContent)
      buildSymlink <- runNixBuild(workDir)
      nixPackagePath <- buildSymlink.canonicalPath.map(ZFileSystem.dir)
    } yield FullInstallResults(nixPackagePath, buildSymlink)
  }


  def javaLauncherContentT: Task[String] = {
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
    } yield {
      if ( launcherConfigOnly )
        launcherConfig(resolutionResponse, artifacts)
      else
        defaultDotNix(resolutionResponse, artifacts)
    }
  }

  def quote(value: String): String = {
    val q = '"'
    s"${q}${value}${q}"
  }

  def quote(values: List[String]): String = {
    s"[${values.map(quote).mkString(" ")}]"
  }

  def quote(value: Option[String]): String = {
    value
      .map(quote)
      .getOrElse("null")
  }

  def defaultDotNix(resolutionResponse: ResolutionResponse, artifacts: Iterable[(ArtifactResponse,NixHash)]): String = {
    def expr(value: String) = { "${" + value + "}" }
    s"""
{
  fetchurl,
  linkFarm,
  jdk8,
  jdk11,
  jdk17,
  stdenv,
  unzip,
}:

  let

    launcherConfig = 
      ${launcherConfig(resolutionResponse, artifacts).trim.indent("      ").trim};

    webappExplode = if launcherConfig.webappExplode == null then false else launcherConfig.webappExplode;

    fetcherFn = 
      dep: (
        fetchurl {
          url = dep.url;
          sha256 = dep.sha256;
        }
      );

    javaVersion = launcherConfig.javaVersion;

    jdk = 
      if javaVersion == null then jdk11
      else if javaVersion == "8" then jdk8
      else if javaVersion == "11" then jdk11
      else if javaVersion == "17" then jdk17
      else abort("expected javaVersion = [ 8 | 11 | 17 ] got $${javaVersion}")
    ;

    artifacts = map fetcherFn launcherConfig.dependencies;

    linkFarmEntryFn = drv: { name = drv.name; path = drv; };

    classpathBuilder = linkFarm launcherConfig.name (map linkFarmEntryFn artifacts);

    args = builtins.concatStringsSep " " (launcherConfig.jvmArgs ++ [launcherConfig.mainClass] ++ launcherConfig.args);

    webappExploder = 
      if webappExplode then
        ''
          echo exploding webapp-composite folder
          for jar in $${classpathBuilder}/*.jar
          do
            $${unzip}/bin/unzip $$jar "webapp/*" -d $$out/webapp-composite 2> /dev/null 1> /dev/null || true
          done
        ''
      else
        ""
    ;

  in

    stdenv.mkDerivation {
      name = launcherConfig.name;
      src = ./.;
      installPhase = ''

        mkdir -p $$out/bin

        # create link to jdk bin so that top and other tools show the process name as something meaningful
        ln -s $${jdk}/bin/java $$out/bin/$${launcherConfig.name}j

        # create link to lib folder derivation
        ln -s $${classpathBuilder} $$out/lib

        LAUNCHER=$$out/bin/$${launcherConfig.name}

        # setup launcher script
        cp ./java-launcher-template $$LAUNCHER
        chmod +x $$LAUNCHER
        substituteInPlace $$LAUNCHER \\
          --replace _name_ $${launcherConfig.name} \\
          --replace _out_ $$out \\
          --replace _args_ "$${args}"

      '' + webappExploder;
    }

""".trim + "\n"
  }

  def launcherConfig(resolutionResponse: ResolutionResponse, artifacts: Iterable[(ArtifactResponse,NixHash)]): String = {
    val content =
      s"""
{

  name = ${quote(parms.name)};
  mainClass = ${quote(parms.mainClass)};
  jvmArgs = ${quote(parms.jvmArgs)};
  args =  ${quote(parms.args)};
  repo = ${quote(parms.repo.value)};
  organization = ${quote(parms.organization)};
  artifact = ${quote(parms.artifact)};
  version = ${quote(parms.version)};
  branch = ${quote(parms.branch.map(_.value))};
  webappExplode = ${parms.webappExplode.getOrElse("null")};
  javaVersion = ${quote(parms.javaVersion)};

  dependencies = [
${
  artifacts
    .map(t => fetchLine(t._1, t._2))
    .mkString("\n")
    .indent("    ")
}
  ];
}
  """.trim
    content
  }

}
