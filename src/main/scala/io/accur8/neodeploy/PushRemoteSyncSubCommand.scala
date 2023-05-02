package io.accur8.neodeploy


import a8.shared.app.{BootstrappedIOApp, LoggingF}
import a8.shared.app.BootstrappedIOApp.BootstrapEnv
import io.accur8.neodeploy.model.{ApplicationName, ServerName, UserLogin}
import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedRepository, ResolvedServer, ResolvedUser}
import zio.{Chunk, Task, UIO, ZIO}
import a8.shared.SharedImports.*
import zio.process.CommandError

import scala.util.Try
import a8.shared.ZFileSystem
import a8.shared.ZString.ZStringer
import io.accur8.neodeploy.PushRemoteSyncSubCommand.{Filter, UserLevelSyncCommand}
import io.accur8.neodeploy.Sync.SyncName

object PushRemoteSyncSubCommand extends LoggingF {

  object Filter {
    def allowAll[A : ZStringer](implicit canEqual: CanEqual[A,A]) = Filter[A]("", Vector.empty)
  }

  case class Filter[A : ZStringer](argName: String, values: Iterable[A]) {

    def hasValues = values.nonEmpty

    def args =
      values
        .nonEmpty
        .toOption(
          Some("--" + argName) ++ values.map(v => implicitly[ZStringer[A]].toZString(v).toString())
        )
        .toVector
        .flatten

    def include(a: A): Boolean =
      matches(a)

    def matches(a: A): Boolean = {
      val r = values.isEmpty || values.find(_.equals(a)).nonEmpty
//      logger.debug(s"matches ${a} -> ${r} -- ${values}")
      r
    }

  }

  sealed abstract class UserLevelSyncCommand(val command: String)
  case object UserSettingsSync extends UserLevelSyncCommand("local_user_sync")
  case object ApplicationSync extends UserLevelSyncCommand("local_app_sync")

}

case class PushRemoteSyncSubCommand(
  resolvedRepository: ResolvedRepository,
  runner: Runner,
  userLevelSyncCommand: UserLevelSyncCommand,
) {

  import runner._

  lazy val validateParameters =
    if ( serversFilter.hasValues || usersFilter.hasValues || appsFilter.hasValues ) {
      ZIO.unit
    } else {
      ZIO.fail(new RuntimeException("must supply servers, users or apps"))
    }

  lazy val validateRepo = ValidateRepo(resolvedRepository)

  lazy val fitleredServers =
    resolvedRepository
      .servers
      .filter(s => serversFilter.include(s.name))

  lazy val run: Task[Unit] =
    for {
      _ <- validateParameters
      _ <- validateRepo.run
      _ <-
        ZIO.collectAllPar(
          fitleredServers
            .map(pushRemoteServerSync)
        )
    } yield ()

  def pushRemoteServerSync(resolvedServer: ResolvedServer): Task[Vector[Command.Result]] = {
    val filteredUsers = resolvedServer.resolvedUsers.filter(u => usersFilter.include(u.login))
    ZIO.collectAll(
      filteredUsers
        .map(pushRemoteUserLevelSync)
    )
  }

  def copyManagedPublicKeysToStagingEffect(stagingDir: ZFileSystem.Directory): Task[Unit] = {
    val publicKeysDir = stagingDir.subdir("public-keys")
    val writes =
      resolvedRepository.allUsers.map { user =>
        for {
          _ <- publicKeysDir.makeDirectories
          publicKeyOpt <- user.publicKey
          _ <-
            publicKeyOpt
              .map { publicKey =>
                user
                  .qualifiedUserNames
                  .map(qualifiedUserName =>
                    publicKeysDir
                      .file(qualifiedUserName.value)
                      .write(publicKey.value)
                  ).sequencePar
              }
              .getOrElse(zunit)
        } yield ()
      }
    writes
      .sequencePar
      .as(())
  }


  def pushRemoteUserLevelSync(resolvedUser: ResolvedUser): Task[Command.Result] = {

    val remoteServer = resolvedUser.server.name

    val stagingDir = resolvedRepository.gitRootDirectory.subdir(s".staging/${resolvedUser.qname}")

    val stagingPath =
      if ( resolvedUser.login === UserLogin.root ) {
        z"${remoteServer}"
      } else {
        z"${remoteServer}/${resolvedUser.login}"
      }

    val setupStagingDataEffect =  
      FileSystemAssist.FileSet(resolvedRepository.gitRootDirectory.unresolved)
        .addPath("config.hocon")
        .addPath("public-keys")
        .addPath(z"${stagingPath}")
        .copyTo(stagingDir)

    val rsyncEffect =
      Command("rsync", "--delete", "--archive", "--verbose", "--recursive", ".", z"${resolvedUser.sshName}:server-app-configs/")
        .workingDirectory(stagingDir)
        .execLogOutput

    val sshEffect =
      Command("ssh", z"${resolvedUser.sshName}", "--")
        .appendArgs(resolvedUser.a8VersionsExec)
        .appendArgsSeq(remoteDebug.toOption("--debug"))
        .appendArgsSeq(remoteTrace.toOption("--trace"))
        .appendArgs(userLevelSyncCommand.command)
        .appendArgsSeq(appsFilter.args)
        .appendArgsSeq(syncsFilter.args)
        .execLogOutput

    (
      stagingDir.deleteChildren
        *> copyManagedPublicKeysToStagingEffect(stagingDir)
        *> setupStagingDataEffect
        *> rsyncEffect
        *> sshEffect
    )
      .logError(s"pushRemoteUserSync(${resolvedUser.qname}) failed")

  }

}
