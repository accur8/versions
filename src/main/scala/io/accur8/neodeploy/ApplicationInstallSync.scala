package io.accur8.neodeploy


import a8.appinstaller.AppInstallerConfig.LibDirKind
import a8.shared.{CompanionGen, Exec, ZFileSystem}
import a8.shared.ZFileSystem.{Directory, File, Path, dir, symlink}
import a8.shared.SharedImports._
import a8.shared.app.{Logging, LoggingF}
import a8.versions.GenerateJavaLauncherDotNix.FullInstallResults
import a8.versions.RepositoryOps.RepoConfigPrefix
import coursier.core.{ModuleName, Organization}
import io.accur8.neodeploy.ApplicationInstallSync.Installer
import io.accur8.neodeploy.model.Install.JavaApp
import io.accur8.neodeploy.model.{ApplicationDescriptor, AppsRootDirectory, Install, Version}
import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedServer, ResolvedUser}
import zio.{Task, ZIO}
import a8.versions.{GenerateJavaLauncherDotNix, RepositoryOps}
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemState.JavaAppInstall
import io.accur8.neodeploy.systemstate.SystemStateModel.M

import java.nio.file.Paths

object ApplicationInstallSync extends Logging with LoggingF {

  case class Installer(installState: SystemState.JavaAppInstall) {

    lazy val gitAppDirectory = installState.gitAppDirectory

    lazy val appDir = installState.appInstallDir

    def applicationDescriptor: ApplicationDescriptor =
      installState.descriptor

    def appRootBinDir = appDir.parentOpt.get.subdir("bin")

    def deleteAppDir: Task[Unit] =
      appDir.delete

    def createAppDir: Task[Unit] =
      appDir.makeDirectories

    def runInstall(installMethod: Install): M[Unit] =
      installMethod match {

        case r: JavaApp =>
          runJavaLauncherInstallDotNix(r)

        case _: Install.Manual =>
          zunit

        case Install.Docker =>
          zunit

      }

    def runJavaLauncherInstallDotNix(repo: JavaApp): M[Unit] = {

      val nixInstallWorkDir = appDir.subdir(s"nix-build")

      val logsDir: Directory = appDir.subdir("logs")
      val tempDir: Directory = appDir.subdir("temp")

      val jvmArgs =
        (repo.jvmArgs ++ List(z"-Dlog.dir=${logsDir}", z"-Djava.io.tmpdir=${tempDir}", z"-Dapp.name=${applicationDescriptor.name}" ))
          .toList

      val request =
        GenerateJavaLauncherDotNix.Parms(
          name = applicationDescriptor.name.value,
          mainClass = repo.mainClass,
          jvmArgs = jvmArgs,
          args = repo.appArgs.toList,
          repo = repo.repository.getOrElse(RepoConfigPrefix.default),
          organization = repo.organization.value,
          artifact = repo.artifact.value,
          version = Some(repo.version.value),
          branch = None,
          webappExplode = Some(repo.webappExplode),
          javaVersion = Some(repo.javaVersion.value.toString),
        )

      val runNixInstaller: Task[FullInstallResults] =
        GenerateJavaLauncherDotNix(
          request,
          false,
        ).runFullInstall(nixInstallWorkDir)

      def linkToNixStore(name: String, installResults: FullInstallResults) = {
        val nixStorePath = installResults.nixPackageInStore.subdir(name)

        def removeExistingSymlinkIfExists: Task[Boolean] = {
          val effect: Task[Either[Throwable, Boolean]] =
            zblock {
              import java.nio.file.Files
              val symLinkNioPath = appDir.symlink(name).asNioPath
              if ( Files.isSymbolicLink(symLinkNioPath) ) {
                Files.delete(symLinkNioPath)
                Right(true)
              } else if ( symLinkNioPath.exists() ) {
                Left(new RuntimeException("" + symLinkNioPath.toFile.getAbsolutePath + " exists but is not a symlink")): Either[Throwable, Boolean]
              } else {
                Right(false)
              }
            }
          effect
            .flatMap(e => ZIO.fromEither(e))
        }

        for {
          exists <- nixStorePath.exists
          _ <- removeExistingSymlinkIfExists
          _ <-
            if ( exists ) {
              appDir
                .symlink(name)
                .writeTarget(nixStorePath.absolutePath)
            } else {
              zunit
            }
        } yield ()
      }

      def createGcRoot = {
        for {
          user <- zservice[ResolvedUser]
          gcroot = ZFileSystem.symlink(z"/nix/var/nix/gcroots/per-user/${user.login}/java-app-${applicationDescriptor.name}")
          _ <- zblock(java.nio.file.Files.deleteIfExists(gcroot.asNioPath))
          _ <- gcroot.writeTarget(nixInstallWorkDir.subdir("build").absolutePath)
        } yield()
      }

      for {
        _ <- nixInstallWorkDir.resolve
        _ <- loggerF.debug(s"runJavaLauncherInstallDotNix(${repo})")
        installResults <- runNixInstaller
        _ <- logsDir.makeDirectories
        _ <- tempDir.makeDirectories
        _ <- linkToNixStore("bin", installResults)
        _ <- linkToNixStore("lib", installResults)
        _ <- linkToNixStore("webapp-composite", installResults)
        _ <- createGcRoot
      } yield ()

    }

    def symlinkConfig: Task[Unit] =
      updateSymLink(installState.gitAppDirectory, appDir.file("config"))

    def updateSymLink(
      target: Path,
      link: File,
    ): Task[Unit] = {
      for {
        _ <- PathAssist.symlink(target, link, deleteIfExists = false)
      } yield ()
    }

    def installAction: M[Unit] =
      for {
        _ <- createAppDir
        _ <- runInstall(applicationDescriptor.install)
        _ <- symlinkConfig
      } yield ()

  }

}

case class ApplicationInstallSync(appsRootDirectory: AppsRootDirectory) extends ApplicationSync with LoggingF {

  // Install
  //    create app directory
  //    symlink git directory to the app/config
  //    create lib directory

  // Update
  //     update lib directory

  // Uninstall
  //     remove app directory

  override val name: Sync.SyncName = Sync.SyncName("installer")


  override def systemState(input: ResolvedApp): M[SystemState] =
    zsucceed(rawSystemState(input))

  def rawSystemState(resolvedApp: ResolvedApp): SystemState =
    resolvedApp.descriptor.install match {
      case Install.Docker =>
        SystemState.Empty
      case fr: JavaApp =>
        SystemState.JavaAppInstall(
          gitAppDirectory = resolvedApp.gitDirectory,
          descriptor = resolvedApp.descriptor,
          appInstallDir = appsRootDirectory.subdir(resolvedApp.descriptor.name.value),
          fromRepo = fr,
        )
      case _: Install.Manual =>
        SystemState.Empty
    }

}
