package io.accur8.neodeploy.systemstate


import a8.shared.ZFileSystem
import io.accur8.neodeploy.systemstate.SystemStateModel._
import a8.shared.SharedImports._

import a8.Scala3Hacks.*
import io.accur8.neodeploy.VFileSystem

trait TextFileContentsMixin extends SystemStateMixin {

  val perms: UnixPerms
  def file: VFileSystem.File

  def contents: String

  def prefix: String

  override def stateKey: Option[StateKey] = StateKey("text file", file.path).some

  override def dryRunInstall: Vector[String] = {
    val permsStr =
       if ( perms.value.nonEmpty )
         s" with ${perms}"
       else
        ""
    Vector(s"${prefix}file ${file}${permsStr}")
  }

  override def isActionNeeded = {
    for {
      zfile <- file.zfile
      permissionActionNeeded0 <- SystemStateImpl.permissionsActionNeeded(file, perms)
      actualContentsOpt <- zfile.readAsStringOpt
    } yield {
      val contentsMatch = actualContentsOpt === some(contents)
      permissionActionNeeded0 || !contentsMatch
    }
  }


  override def runApplyNewState: M[Unit] = {
    for {
      zfile <- file.zfile
      parentExists <- zfile.parent.exists
      _ <-
        if (parentExists)
          zunit
        else
          zfile.parent.makeDirectories
      _ <- zfile.write(contents)
      _ <-
        if ( perms.value.nonEmpty ) {
          io.accur8.neodeploy.systemstate.SystemStateModel.Command("chmod", perms.value, zfile.absolutePath)
            .execCaptureOutput
        } else {
          zunit
        }
    } yield ()
  }


  override def runUninstallObsolete(interpreter: Interpreter) =
    file
      .zfile
      .flatMap(_.delete)

}
