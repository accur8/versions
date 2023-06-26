package io.accur8.neodeploy


import a8.shared.{Exec, StringValue}
import io.accur8.neodeploy.model.{DomainName, Install, Version}
import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedRepository}
import zio.Task
import a8.shared.SharedImports.*
import a8.shared.app.LoggingF
import a8.versions.{ParsedVersion, RepositoryOps, VersionParser}
import a8.versions.model.{RepoPrefix, ResolutionRequest}
import io.accur8.neodeploy.PushRemoteSyncSubCommand.Filter
import io.accur8.neodeploy.Sync.SyncName
import io.accur8.neodeploy.systemstate.SystemState.JavaAppInstall

object DeploySubCommand {
}

case class DeploySubCommand(
  resolvedRepository: ResolvedRepository,
  runner: Runner,
  rawVersion: Version,
  appName: DomainName,
)
  extends LoggingF
{

  def run: Task[Unit] = {

    // find the apps to deploy
    def deployEachAppEffect =
      resolvedRepository
        .applications
        .filter(_.isNamed(appName))
        .map(ra => deployApp(ra))
        .sequence

    for {
      versions <- deployEachAppEffect
      _ <- gitCommit(versions.head)
      _ <- gitPush
    } yield ()

  }

  def resolveVersion(app: ResolvedApp): Task[Version] =
    rawVersion.value.toLowerCase.trim match {
      case "latest" =>
        app.loadedApplicationDescriptor.descriptor.install match {
          case ja: Install.JavaApp =>
            zblock {
              val parsedVersion = ParsedVersion.parse(ja.version.value).get
              val resolutionRequest =
                ResolutionRequest(
                  repoPrefix = ja.repository.getOrElse(RepositoryOps.default.repoConfigPrefix),
                  organization = ja.organization,
                  artifact = ja.artifact,
                  version = Version("latest"),
                  branch = parsedVersion.buildInfo.get.branch.some,
                )
              val resolutionResponse = RepositoryOps.runResolve(resolutionRequest)
              resolutionResponse.version
            }
          case _ =>
            zfail(new RuntimeException(s"latest is only valid on java apps not ${app.loadedApplicationDescriptor.descriptor.install}"))
        }
      case "current" =>
        app.loadedApplicationDescriptor.descriptor.install match {
          case ja: Install.JavaApp =>
            zsucceed(ja.version)
          case _ =>
            zfail(new RuntimeException(s"current is only valid on java apps not ${app.loadedApplicationDescriptor.descriptor.install}"))
        }
      case _ =>
        zsucceed(rawVersion)
    }

  def gitCommit(version: Version): Task[Unit] =
    zblock(
      Exec("git", "commit", "-am", z"deploy --version ${version} --app ${appName}")
        .inDirectory(a8.shared.FileSystem.dir(resolvedRepository.gitRootDirectory.unresolved.absolutePath))
        .execInline(): @scala.annotation.nowarn
    )

  def gitPush: Task[Unit] =
    zblock(
      Exec("git", "push")
        .inDirectory(a8.shared.FileSystem.dir(resolvedRepository.gitRootDirectory.unresolved.absolutePath))
        .execInline(): @scala.annotation.nowarn
    )

  def deployApp(app: ResolvedApp): Task[Version] = {

    val resolvedRunner =
      runner
        .copy(
          serversFilter = Filter("server", Iterable(app.server.name)),
          usersFilter = Filter("user", Iterable(app.user.login)),
          appsFilter = Filter("app", Iterable(app.name)),
        )

    val versionDotPropsFile = app.gitDirectory.file("version.properties")

    resolveVersion(app).flatMap { version =>

      // set the version
      val setVersionEffect =
        versionDotPropsFile
          .write(z"""# ${a8.shared.FileSystem.fileSystemCompatibleTimestamp()}${"\n"}version_override=${version}""")

      val runPushRemoteSyncEffect =
        PushRemoteSyncSubCommand(
          resolvedRepository,
          resolvedRunner,
          PushRemoteSyncSubCommand.ApplicationSync,
        ).run

      versionDotPropsFile.readAsStringOpt.flatMap { savedVersionDotProps =>
        val effect =
          for {
            _ <- setVersionEffect
            _ <- runPushRemoteSyncEffect
          } yield version
        effect
          .onError { _ =>
            val revertEffect =
              savedVersionDotProps match {
                case None =>
                  versionDotPropsFile.delete
                case Some(content) =>
                  versionDotPropsFile.write(content)
              }
            for {
              _ <- loggerF.info(s"deploy failed reverting ${versionDotPropsFile}")
              _ <- revertEffect.logVoid
            } yield ()
          }
      }
    }

  }

}
