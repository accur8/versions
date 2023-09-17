package io.accur8.neodeploy.systemstate


import a8.shared.SharedImports.*
import a8.common.logging.LoggingF
import a8.shared.{FileSystem, ZFileSystem}
import io.accur8.neodeploy.Installer
import io.accur8.neodeploy.model.{ApplicationDescriptor, AppsInfo, AppsRootDirectory}
import io.accur8.neodeploy.systemstate.SystemStateModel.*
import zio.ZIO

import java.nio.file.Files
import a8.Scala3Hacks.*

trait JavaAppInstallMixin extends SystemStateMixin with LoggingF { self: SystemState.JavaAppInstall =>

  lazy val rawStateKey = StateKey("app install", canonicalAppDir.absolutePath)

  override def stateKey: Option[StateKey] = rawStateKey.some
  override def dryRunInstall: Vector[String] = Vector(s"app install into ${canonicalAppDir} -- ${self.fromRepo.compactJson}")

  lazy val applicationDotJsonFile =
    canonicalAppDir
      .asDirectory
      .file("application.json")


  /*
   * because java app installs are lightweight now we will always install
   */
  override def isActionNeeded =
    zsucceed(true)

//  /**
//   * if the installed application descriptor matches the descriptor then
//   * no install is needed, otherwise an install is needed
//   */
//  override def isActionNeeded =
//    applicationDotJsonFile
//      .readAsStringOpt
//      .flatMap {
//        case None =>
//          zsucceed(None)
//        case Some(s) =>
//          json.readF[ApplicationDescriptor](s)
//            .map(_.some)
//      }
//      .either
//      .map {
//        case Left(e) =>
//          true
//        case Right(None) =>
//          true
//        case Right(Some(installedDescriptor)) =>
//          descriptor != installedDescriptor
//      }

  override def runApplyNewState = {
    for {
      appsInfo <- zservice[AppsInfo]
      runTimestamp <- zservice[RunTimestamp]
      _ <- Installer(this, appsInfo, runTimestamp).installAction
    } yield ()
  }

  override def runUninstallObsolete(interpreter: Interpreter) =
    if ( !interpreter.newStatesByKey.contains(rawStateKey) && interpreter.previousStatesByKey.contains(rawStateKey) ) {
      canonicalAppDir.deleteIfExists
    } else {
      zunit
    }


  override def dryRunUninstall(interpreter: Interpreter): Vector[String] =
    if ( !interpreter.newStatesByKey.contains(rawStateKey) && interpreter.previousStatesByKey.contains(rawStateKey) ) {
      Vector("uninstall " + canonicalAppDir.absolutePath)
    } else {
      Vector.empty
    }


}
