package io.accur8.neodeploy.systemstate


import a8.shared.SharedImports._
import a8.shared.{FileSystem, ZFileSystem}
import io.accur8.neodeploy.ApplicationInstallSync.Installer
import io.accur8.neodeploy.model.ApplicationDescriptor
import io.accur8.neodeploy.systemstate.SystemStateModel._
import zio.ZIO

import java.nio.file.Files

trait JavaAppInstallMixin extends SystemStateMixin { self: SystemState.JavaAppInstall =>

  override def stateKey: Option[StateKey] = StateKey("app install", appInstallDir.absolutePath).some
  override def dryRunInstall: Vector[String] = Vector(s"app install into ${appInstallDir} -- ${self.fromRepo.compactJson}")

  lazy val applicationDotJsonFile =
    appInstallDir
      .subdir("config")
      .file("application.json")


  /**
   * if the installed appliciation descriptor matches the descriptor then
   * no install is needed, otherwise an install is needed
   */
  override def isActionNeeded =
    applicationDotJsonFile
      .readAsStringOpt
      .flatMap {
        case None =>
          zsucceed(None)
        case Some(s) =>
          json.readF[ApplicationDescriptor](s)
            .map(_.some)
      }
      .either
      .map {
        case Left(e) =>
          true
        case Right(None) =>
          true
        case Right(Some(installedDescriptor)) =>
          descriptor !== installedDescriptor
      }

  override def runApplyNewState = {
    for {
      _ <- Installer(this).installAction
      _ <- applicationDotJsonFile.write(descriptor.prettyJson)
    } yield ()
  }


  override def runUninstallObsolete = {
    val aid = appInstallDir
    val backupDir = aid.parentOpt.get.subdir("_backups").subdir(aid.name + "-" + FileSystem.fileSystemCompatibleTimestamp())
    for {
      _ <- backupDir.parentOpt.map(_.makeDirectories).getOrElse(zunit)
      _ <- ZIO.attemptBlocking(Files.move(aid.asNioPath, backupDir.asNioPath))
    } yield ()
  }

}
