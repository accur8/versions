package io.accur8.neodeploy


import SharedImports.*
import io.accur8.neodeploy.systemstate.*
import zio.Task
import a8.common.logging.LoggingF
import a8.versions.GenerateJavaLauncherDotNix
import io.accur8.neodeploy.model.*
import io.accur8.neodeploy.model.Install.*
import io.accur8.neodeploy.resolvedmodel.ResolvedUser
import io.accur8.neodeploy.systemstate.SystemStateModel.*
import a8.versions.RepositoryOps.RepoConfigPrefix
import a8.versions.GenerateJavaLauncherDotNix.FullInstallResults
import io.accur8.neodeploy.VFileSystem
import io.accur8.neodeploy.VFileSystem.Directory

case class Installer(installState: SystemState.JavaAppInstall, appsInfo: AppsInfo, runTimestamp: RunTimestamp) extends LoggingF {

  lazy val gitAppDirectory = installState.gitAppDirectory

  lazy val canonicalAppDir = installState.canonicalAppDir

  lazy val installDir: VFileSystem.Directory =
    appsInfo
      .installsDir
      .subdir(z"${installState.descriptor.name}-${runTimestamp.asFileSystemCompatibleStr}")

  lazy val persistenceDir: VFileSystem.Directory =
    appsInfo
      .persistenceDir
      .subdir(installState.descriptor.name.value)

  def applicationDescriptor: ApplicationDescriptor =
    installState.descriptor

  def createInstallDir: N[Unit] =
    installDir
      .makeDirectories
      .as(())

  def linkChildrenIntoInstallDir(source: Directory): N[Unit] = {
    import a8.shared.ZFileSystem.SymlinkHandlerDefaults.noFollow
    source
      .entries
      .flatMap { entries =>
        entries
          .map { e =>
            val link = installDir.symlink(e.name)
            link
              .exists
              .flatMap {
                case true =>
                  loggerF.info(s"unable to link ${e} into ${link} since it already exists")
                case false =>
                  link.writeTarget(e)
              }
          }
          .toSeq
          .sequencePar
      }
      .as(())
  }

  def runInstall(installMethod: Install): M[Unit] =
    installMethod match {

      case r: JavaApp =>
        runJavaLauncherInstallDotNix(r)

      case _: Install.Manual =>
        zunit

      case Install.Docker =>
        zunit

    }

  def runJavaLauncherInstallDotNix(repo: JavaApp): M[Unit] = zservice[PathLocator].flatMap { implicit pathLocator =>

    val nixInstallWorkDir = installDir.subdir(s"nix-build")

    val logsDirInInstall: Directory = installDir.subdir("logs")
    val tempDirInInstall: Directory = installDir.subdir("temp")

    val setupPersistentDir: M[Unit] = {
      persistenceDir
        .existsAsDirectory
        .flatMap {
          case true =>
            zunit
          case false =>
            for {
              _ <- persistenceDir.subdir("logs").makeDirectories
              _ <- persistenceDir.subdir("data").makeDirectories
              _ <- persistenceDir.subdir("cache").makeDirectories
              _ <- persistenceDir.subdir("temp").makeDirectories
            } yield ()
        }
    }

    val jvmArgs =
      (repo.jvmArgs ++ List(z"-Dlog.dir=${logsDirInInstall.absPath}", z"-Djava.io.tmpdir=${tempDirInInstall.absPath}", z"-Dapp.name=${applicationDescriptor.name}"))
        .toList

    val request =
      GenerateJavaLauncherDotNix.Parms(
        name = applicationDescriptor.name.value,
        mainClass = repo.mainClass,
        jvmArgs = jvmArgs,
        args = repo.appArgs.toList,
        repo = repo.repository.getOrElse(RepoConfigPrefix.default),
        organization = repo.organization,
        artifact = repo.artifact,
        version = Some(repo.version),
        branch = None,
        webappExplode = Some(repo.webappExplode),
        javaVersion = Some(repo.javaVersion.value.toString),
      )

    val runNixInstaller: N[FullInstallResults] =
      GenerateJavaLauncherDotNix(request, appsInfo.nixHashCacheDir.some)
        .runFullInstall(nixInstallWorkDir)

    //      def linkToNixStore(name: String, installResults: FullInstallResults) = {
    //        val nixStorePath = installResults.nixPackageInStore.subdir(name)
    //
    //        def removeExistingSymlinkIfExists: Task[Boolean] = {
    //          val effect: Task[Either[Throwable, Boolean]] =
    //            zblock {
    //              import java.nio.file.Files
    //              val symLinkNioPath = installDir.symlink(name).asNioPath
    //              if ( Files.isSymbolicLink(symLinkNioPath) ) {
    //                Files.delete(symLinkNioPath)
    //                Right(true)
    //              } else if ( symLinkNioPath.exists() ) {
    //                Left(new RuntimeException("" + symLinkNioPath.toFile.getAbsolutePath + " exists but is not a symlink")): Either[Throwable, Boolean]
    //              } else {
    //                Right(false)
    //              }
    //            }
    //          effect
    //            .flatMap(e => ZIO.fromEither(e))
    //        }
    //
    //        for {
    //          exists <- nixStorePath.exists
    //          _ <- removeExistingSymlinkIfExists
    //          _ <-
    //            if ( exists ) {
    //              installDir
    //                .symlink(name)
    //                .writeTarget(nixStorePath.absolutePath)
    //            } else {
    //              zunit
    //            }
    //        } yield ()
    //      }

    def createGcRoot = {
      for {
        user <- zservice[UserLogin]
        gcroot = VFileSystem.link(z"/nix/var/nix/gcroots/per-user/${user}/java-app-${applicationDescriptor.name}")
        _ <- gcroot.deleteIfExists
        _ <- gcroot.writeTarget(nixInstallWorkDir.subdir("build"))
      } yield ()
    }

    for {
      _ <- installDir.makeDirectories
      _ <- nixInstallWorkDir.resolve
      _ <- loggerF.debug(s"runJavaLauncherInstallDotNix(${repo})")
      installResults <- runNixInstaller
      _ <- setupPersistentDir
      _ <- linkChildrenIntoInstallDir(installResults.nixPackageInStore)
      _ <- linkChildrenIntoInstallDir(persistenceDir)
      _ <- createGcRoot
      _ <- installDir.file("application.json").write(installState.descriptor.prettyJson)
    } yield ()

  }

  def copyConfig: N[Unit] = {
    val configDir = installDir.subdir("config")
    (
      configDir.makeDirectories
        *> installState.gitAppDirectory.copyChildrenTo(configDir)
    )
  }

  def linkInstallDirToCanonicalDir: M[Unit] = {
    import a8.shared.ZFileSystem.SymlinkHandlerDefaults.noFollow
    canonicalAppDir
      .exists
      .flatMap {
        case false =>
          zunit
        case true =>
          loggerF.debug(s"removing old app link ${canonicalAppDir}")
            .asZIO(canonicalAppDir.delete)
      }
      .asZIO(
        loggerF.debug(s"writing new app link ${canonicalAppDir} -> ${installDir}")
          *> canonicalAppDir.writeTarget(installDir)
      )
  }

  def installAction: M[Unit] =
    for {
      _ <- createInstallDir
      _ <- runInstall(applicationDescriptor.install)
      _ <- copyConfig
      _ <- installState.stopService.runApplyNewState
      _ <- linkInstallDirToCanonicalDir
      _ <- installState.startService.runApplyNewState
    } yield ()

}

