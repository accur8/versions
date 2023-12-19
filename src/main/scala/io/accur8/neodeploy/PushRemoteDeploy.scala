package io.accur8.neodeploy


import a8.shared.app.BootstrappedIOApp
import a8.shared.app.BootstrappedIOApp.BootstrapEnv
import io.accur8.neodeploy.model.{ApplicationName, ServerName, UserLogin}
import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedRepository, ResolvedServer, ResolvedUser}
import zio.{Chunk, Task, UIO, ZIO}
import SharedImports.*
import zio.process.CommandError

import scala.util.Try
import a8.shared.ZFileSystem
import a8.shared.ZString.ZStringer
import io.accur8.neodeploy.systemstate.SystemStateModel.Command

object PushRemoteDeploy extends LoggingF {

}

case class PushRemoteDeploy(
  resolvedRepository: ResolvedRepository,
  runner: Runner,
  user: ResolvedUser,
  args: Iterable[String],
) {

  import runner._

  lazy val run: N[Unit] =
    pushRemoteUserLevelSync(user)
      .as(())

  def copyManagedPublicKeysToStagingEffect(stagingDir: VFileSystem.Directory): N[Unit] = {
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


  def pushRemoteUserLevelSync(resolvedUser: ResolvedUser): N[Command.Result] = {

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
        .addFile("config.hocon")
        .addDirectory("public-keys")
        .addDirectory(z"${stagingPath}")
        .copyTo(stagingDir)

    val rsyncEffect =
      Command("rsync", "--delete", "--recursive", "--links", "--perms", "--verbose", "--recursive", ".", z"${resolvedUser.sshName}:server-app-configs/")
        .inDirectory(stagingDir)
        .execLogOutput

    val sshEffect =
      Command("ssh", z"${resolvedUser.sshName}", "--")
        .appendArgs(resolvedUser.a8VersionsExec)
        .appendArgsSeq(remoteDebug.toOption("--debug"))
        .appendArgsSeq(remoteTrace.toOption("--trace"))
        .appendArgs("local_deploy")
        .appendArgsSeq(args)
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
