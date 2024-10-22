package a8.versions


import a8.shared.{CompanionGen, StringValue}
import a8.shared.app.{BootstrappedIOApp}
import a8.shared.app.BootstrappedIOApp.BootstrapEnv
import a8.versions.Build.BuildType
import a8.versions.RepositoryOps.RepoConfigPrefix
import a8.versions.ParsedVersion.BuildInfo
import a8.versions.model.{ArtifactResponse, BranchName, ResolutionRequest, ResolutionResponse}
import coursier.ModuleName
import coursier.core.Module
import io.accur8.neodeploy.HealthchecksDotIo
import zio.{Chunk, LogLevel, ZIO}

import java.io.File
import java.net.URL
import java.net.http.HttpRequest
import java.security.MessageDigest
import java.util.Base64
import scala.util.Try
import io.accur8.neodeploy.SharedImports.*
import a8.versions.GenerateJavaLauncherDotNix.{BuildDescription, FileContents, FullInstallResults}
import a8.versions.MxGenerateJavaLauncherDotNix.*
import io.accur8.neodeploy.model.{Artifact, Organization, Version}
import org.apache.commons.codec.binary.{Base32, Hex}

import io.accur8.neodeploy.PredefAssist.{given, _}
import VFileSystem.{Directory, Symlink}


object GenerateJavaLauncherDotNix extends LoggingF {

  lazy val isNixos = new java.io.File("/etc/NIXOS").exists()


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
    organization: Organization,
    artifact: Artifact,
    version: Option[Version] = None,
    branch: Option[BranchName] = None,
    webappExplode: Option[Boolean] = None,
    javaVersion: Option[String] = None,

    /** this is ignored */
    dependencyDownloader: Option[String] = None,
  ) {

    lazy val coursierModule =
      coursier.Module(organization.asCoursierOrg, artifact.asCoursierModuleName)

    lazy val resolutionRequest: ResolutionRequest =
      ResolutionRequest(
        repoPrefix = repo,
        organization = organization,
        artifact = artifact,
        version = version.getOrElse(io.accur8.neodeploy.model.Version("latest")),
        branch = branch,
      )

  }

  case class FullInstallResults(
    nixPackageInStore: Directory,
    buildOutLink: Symlink,
  )

  object FileContents extends MxFileContents
  @CompanionGen
  case class FileContents(
    filename: String,
    contents: String,
  )

  object BuildDescription extends MxBuildDescription
  @CompanionGen
  case class BuildDescription(
    files: Iterable[FileContents],
    resolvedVersion: io.accur8.neodeploy.model.Version,
    resolutionResponse: ResolutionResponse,
  ) {
    lazy val defaultDotNixContent = files.find(_.filename == "default.nix").get.contents
    lazy val javaLauncherTemplate = files.find(_.filename == "java-launcher-template").get.contents
  }

}


case class GenerateJavaLauncherDotNix(
  parms: GenerateJavaLauncherDotNix.Parms,
  nixHashCacheDir: Option[VFileSystem.Directory],
)
  extends LoggingF
{

  import GenerateJavaLauncherDotNix.isNixos

  logger.info(s"using args ${parms.args}")

  val parallelism = 20

  lazy val repositoryOps = RepositoryOps(parms.resolutionRequest.repoPrefix)

  // hardcoded to use maven for now
  val resolutionResponseZ: N[model.ResolutionResponse] =
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

  def nixHashFromCache(artifactResponse: ArtifactResponse, loadCacheEffect: N[NixHash]): N[NixHash] = {
    import a8.shared.ZFileSystem.SymlinkHandlerDefaults.follow
    nixHashCacheDir
      .map { nhcd =>
        val cacheFile =
          nhcd
            .subdir(artifactResponse.organization.value)
            .file(artifactResponse.filename + ".nixhash")
        cacheFile
          .exists
          .flatMap {
            case true =>
              cacheFile
                .readAsString
                .map(v => NixHash(v.trim))
            case false =>
              loadCacheEffect
                .flatMap(nh =>
                  cacheFile
                    .parent
                    .makeDirectories
                    .asZIO(cacheFile.write(nh.value))
                    .as(nh)
                )
          }
      }
      .getOrElse(loadCacheEffect)
  }

  def nixHash(artifactResponse: ArtifactResponse): N[NixHash] = {
    import artifactResponse.url
    val loadHashEffect =
      nixHashFromRepo(url)
        .flatMap {
          case scala.util.Success(value) =>
            zsucceed(value)
          case _ =>
            nixPrefetchUrl(url)
              .map(_.nixHash)
        }
    nixHashFromCache(artifactResponse, loadHashEffect)
      .traceLog(s"nixHash(${url})")

  }

  def repoAuthHeaders: Seq[(String,String)] =
    repositoryOps
      .remoteRepositoryAuthentication
      .toSeq
      .flatMap(_.httpHeaders)

  def nixHashFromRepo(url: sttp.model.Uri): N[Try[NixHash]] =
    ZIO
      .attemptBlocking {

        val sha256Url = url.toString + ".sha256"

        val request = {
          val t0 =
            java.net.http.HttpRequest
              .newBuilder()
              .uri(java.net.URI.create(sha256Url))
              .GET()

          val headers = repoAuthHeaders

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
      .debugLog(s"nixHashFromRepo(${url})")

  def nixPrefetchUrl(url: sttp.model.Uri): N[NixPrefetchResult] =
    ZIO
      .attemptBlocking {
        import sys.process._
        import scala.language.postfixOps
        val urlWithAuth =
          repositoryOps
            .remoteRepositoryAuthentication
            .flatMap(auth => auth.passwordOpt.map(p => url.userInfo(auth.user,p)))
            .getOrElse(url)

        val exec =
          if ( isNixos ) "nix-prefetch-url"
          else "/nix/var/nix/profiles/default/bin/nix-prefetch-url"

        val results = (s"${exec} --print-path ${urlWithAuth}" !!)
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
      .debugLog(s"nixPrefetchUrl(${url})")


  def runNixBuild(workDir: Directory): N[Symlink] = {
    val exec =
      if (isNixos) "nix-build"
      else "/nix/var/nix/profiles/default/bin/nix-build"

    val symlinkName = "build"
    workDir
      .zdir
      .flatMap(workdDirZ =>
        ZIO.attemptBlocking(
          Exec(
            Seq(exec, "--out-link", symlinkName, "-E", "with import <nixpkgs> {}; (callPackage ./launcher {})"),
            Some(a8.shared.FileSystem.dir(workdDirZ.absolutePath)),
          ).execCaptureOutput()
        ).as(workDir.symlink(symlinkName))
      )
  }

  def runFullInstall(workDir: Directory): N[FullInstallResults] = {
    val launcherFilesDir = workDir.subdir("launcher")
    for {
      buildDescription <- buildDescriptionT
      _ <- launcherFilesDir.file("default.nix").write(buildDescription.defaultDotNixContent)
      _ <- launcherFilesDir.file("java-launcher-template").write(javaLauncherTemplateContent)
      buildSymlink <- runNixBuild(workDir)
    } yield FullInstallResults(buildSymlink.asDirectory, buildSymlink)
  }

  def buildDescriptionT: N[BuildDescription] = {
    for {
      resolutionResponse <- resolutionResponseZ
      artifacts <-
        ZIO.foreachPar(resolutionResponse.artifacts)(a => nixHash(a).map(a -> _))
          .withParallelism(parallelism)
    } yield
      BuildDescription(
        files = Iterable(
          FileContents("default.nix", defaultDotNix(resolutionResponse, artifacts)),
          FileContents("java-launcher-config.nix", launcherConfig(resolutionResponse, artifacts)),
          FileContents("java-launcher-template", javaLauncherTemplateContent),
        ),
        resolvedVersion = resolutionResponse.version,
        resolutionResponse = resolutionResponse,
      )
  }

  def quote(value: String): String = {
    val q = '"'
    s"${q}${value}${q}"
  }

  def quote(stringValue: StringValue): String = {
    val q = '"'
    s"${q}${stringValue.value}${q}"
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
            $${unzip}/bin/unzip $$jar "webapp/*" -d $$out 2> /dev/null 1> /dev/null || true
          done
          if [ -d $$out/webapp ]; then
            mv $$out/webapp $$out/webapp-composite
            mkdir -p $$out/webapp-composite/WEB-INF/tmp-file-upload
          fi
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
  version = ${quote(parms.version.map(_.value))};
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
