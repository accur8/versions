package io.accur8.neodeploy.systemstate


import a8.shared.ZFileSystem
import io.accur8.neodeploy.systemstate.SystemStateModel._
import a8.shared.SharedImports._

import a8.Scala3Hacks.*

trait TextFileContentsMixin extends SystemStateMixin {

  val file: ZFileSystem.File
  val perms: UnixPerms
  def filename = file.absolutePath

  def contents: String

  def prefix: String

  override def stateKey: Option[StateKey] = StateKey("text file", file.absolutePath).some

  override def dryRunInstall: Vector[String] = {
    val permsStr =
       if ( perms.value.nonEmpty )
         s" with ${perms}"
       else
        ""
    Vector(s"${prefix}file ${filename}${permsStr}")
  }

  override def isActionNeeded = {
    val file = ZFileSystem.file(filename)
    for {
      permissionActionNeeded0 <- SystemStateImpl.permissionsActionNeeded(file, perms)
      actualContentsOpt <- file.readAsStringOpt
    } yield {
      val contentsMatch = actualContentsOpt === some(contents)
      permissionActionNeeded0 || !contentsMatch
    }
  }


  override def runApplyNewState = {
    val file = ZFileSystem.file(filename)
    for {
      parentExists <- file.parent.exists
      _ <-
        if (parentExists)
          zunit
        else
          file.parent.makeDirectories
      _ <- file.write(contents)
      _ <-
        if ( perms.value.nonEmpty ) {
          io.accur8.neodeploy.Command("chmod", perms.value, filename)
            .execCaptureOutput
        } else {
          zunit
        }
    } yield ()
  }


  override def runUninstallObsolete(interpreter: Interpreter) =
    ZFileSystem.file(filename).delete

}
