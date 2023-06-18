package io.accur8.neodeploy


import a8.appinstaller.AppInstallerConfig.LibDirKind
import a8.shared.{CompanionGen, Exec, ZFileSystem}
import a8.shared.ZFileSystem.{Directory, File, Path, SymlinkHandler, dir, symlink}
import a8.shared.SharedImports.*
import a8.shared.app.{Logging, LoggingF}
import a8.versions.GenerateJavaLauncherDotNix.FullInstallResults
import a8.versions.RepositoryOps.RepoConfigPrefix
import coursier.core.{ModuleName, Organization}
import io.accur8.neodeploy.model.Install.JavaApp
import io.accur8.neodeploy.model.{ApplicationDescriptor, AppsInfo, AppsRootDirectory, Install, Launcher, Version}
import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedServer, ResolvedUser}
import zio.{Task, ZIO}
import a8.versions.{GenerateJavaLauncherDotNix, RepositoryOps}
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemState.JavaAppInstall
import io.accur8.neodeploy.systemstate.SystemStateModel.{M, RunTimestamp}

import java.nio.file.Paths
import a8.Scala3Hacks.*

object ApplicationInstallSync extends Logging with LoggingF {

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

  def rawSystemState(resolvedApp: ResolvedApp): SystemState = {
    val launcher = Launcher(resolvedApp)
    resolvedApp.descriptor.install match {
      case Install.Docker =>
        launcher.installService
      case fr: JavaApp =>
        val javaAppInstall =
          SystemState.JavaAppInstall(
            gitAppDirectory = resolvedApp.gitDirectory,
            descriptor = resolvedApp.descriptor,
            canonicalAppDir = appsRootDirectory.symlink(resolvedApp.descriptor.name.value),
            fromRepo = fr,
            stopService = launcher.stopService,
            startService = launcher.startService,
          )
        SystemState.Composite(
          "install/update java app",
          states = Vector(
            launcher.installService,
            javaAppInstall,
          )
        )
      case _: Install.Manual =>
        launcher.installService
    }
  }

}
