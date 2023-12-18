package io.accur8.neodeploy


import a8.shared.app.BootstrappedIOApp
import a8.shared.app.BootstrappedIOApp.BootstrapEnv
import zio.{Task, ZIO}
import SharedImports.*
import io.accur8.neodeploy.resolvedmodel.ResolvedRepository
import PredefAssist.*
import a8.Scala3Hacks.*
import io.accur8.neodeploy.systemstate.SystemStateModel.Command

case class ValidateRepo(resolvedRepository: ResolvedRepository) extends LoggingF {

  lazy val gitRootDirectory = resolvedRepository.gitRootDirectory

  lazy val allUsers =
    resolvedRepository
      .allUsers

  def run: Task[Unit] = {
    val rawEffect = setupSshKeys zipPar addGitattributesFile zipPar validatePlugins
    Layers.provideN(rawEffect)
      .as(())
  }

  def validatePlugins =
    ZIO.attemptBlocking {
      allUsers
        .foreach(_.plugins.pluginInstances)
    }

  def setupSshKeys: N[Unit] =
    allUsers
      .map { user =>
        val pkf = user.sshPrivateKeyFileInRepo
        pkf
          .exists
          .map { pkfExists =>
            user -> (user.descriptor.manageSshKeys && !pkfExists)
          }
      }
      .sequence
      .map(
        _.collect {
          case (user, true) =>
            user
        }
      )
      .flatMap { (users: Seq[resolvedmodel.ResolvedUser]) =>
        val setupUsersEffect: Seq[N[Unit]] =
          users
            .map { user =>
              val tempFile = user.tempSshPrivateKeyFileInRepo
              val makeDirectoriesEffect = user.repoDir.makeDirectories
              val sshKeygenEffect =
                Command(
                  "ssh-keygen", "-t", "ed25519", "-a", "100", "-f", z"$tempFile", "-q", "-N", "", "-C", user.qname
                )
                  .inDirectory(user.repoDir)
                  .execLogOutput
                  .asZIO(
                    ZIO.attemptBlocking(
                      tempFile.renameTo(user.sshPrivateKeyFileInRepo)
                    )
                  )
              (makeDirectoriesEffect *> sshKeygenEffect)
                .correlateWith0(s"setup ssh keys for ${user.qname}")
            }
        ZIO.collectAllPar(setupUsersEffect)
          .as(())
      }

  def addGitattributesFile: N[Unit] = {
    val gitAttributesFile = gitRootDirectory.file(".gitattributes")
      gitAttributesFile
        .exists
        .flatMap {
          case true =>
            zunit
          case false =>
            val lines =
              Seq(
                ".gitattributes !filter !diff",
                "**/*.priv filter=git-crypt diff=git-crypt",
                "*.priv filter=git-crypt diff=git-crypt",
              )
            gitAttributesFile.write(lines.mkString("", "\n", "\n"))
      }
  }



}
