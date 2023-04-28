package io.accur8.neodeploy


import a8.shared.{Exec, StringValue}
import io.accur8.neodeploy.model.{DomainName, Version}
import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedRepository}
import zio.Task
import a8.shared.SharedImports.*
import a8.shared.app.LoggingF
import io.accur8.neodeploy.PushRemoteSyncSubCommand.Filter
import io.accur8.neodeploy.Sync.SyncName

object DeploySubCommand {
}

case class DeploySubCommand(
  resolvedRepository: ResolvedRepository,
  runner: Runner,
  version: Version,
  appName: DomainName,
)
  extends LoggingF
{

  def run: Task[Unit] = {

    // todo: get overloaded properties working

//    ??? // git pull
//    ??? // run neo deploy runner to load resolvedRepository

    // find the apps to deploy
    val deployEachAppEffect =
      resolvedRepository
        .applications
        .filter(_.isNamed(appName))
        .map(deployApp)
        .sequence

    for {
      _ <- deployEachAppEffect
      _ <- gitCommit
      _ <- gitPush
    } yield ()

  }

  def gitCommit: Task[Unit] =
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

  def deployApp(app: ResolvedApp): Task[Unit] = {

    val resolvedRunner =
      runner
        .copy(
          serversFilter = Filter("server", Iterable(app.server.name)),
          usersFilter = Filter("user", Iterable(app.user.login)),
          syncsFilter = Filter("sync", Iterable(SyncName("installer"))),
          appsFilter = Filter("app", Iterable(app.name)),
        )

    val versionDotPropsFile = app.gitDirectory.file("version.properties")

    // set the version
    val setVersionEffect =
      versionDotPropsFile
        .write(z"""# ${a8.shared.FileSystem.fileSystemCompatibleTimestamp()}${"\n"}version_override=${version}""")

    val runPushRemoteSyncEffect =
      // push_remote_sync --server server --user user --app app --sync installer
      PushRemoteSyncSubCommand(
        resolvedRepository,
        resolvedRunner,
      ).run

    versionDotPropsFile.readAsStringOpt.flatMap { savedVersionDotProps =>
      val effect =
        for {
          _ <- setVersionEffect
          _ <- runPushRemoteSyncEffect
        } yield ()
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
