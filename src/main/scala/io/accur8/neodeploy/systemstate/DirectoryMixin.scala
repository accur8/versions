package io.accur8.neodeploy.systemstate

import a8.shared.SharedImports._
import a8.shared.ZFileSystem
import io.accur8.neodeploy.systemstate.SystemStateModel._

import a8.Scala3Hacks.*

trait DirectoryMixin extends SystemStateMixin { self: SystemState.Directory =>

  override def stateKey: Option[StateKey] = StateKey("directory", path.absolutePath).some
  override def dryRunInstall: Vector[String] = Vector(z"directory ${path} with ${perms}")

  override def isActionNeeded = {
    SystemStateImpl.permissionsActionNeeded(path, perms)
  }

  override def runApplyNewState = {
    for {
      exists <- path.exists
      _ <-
        if ( exists ) {
          zunit
        } else {
          path.makeDirectories
        }
      _ <- SystemStateImpl.applyPermissions(path, perms)
    } yield ()
  }

  override def runUninstallObsolete = {

    for {
      entries <- path.entries
      _ <-
        if (entries.isEmpty) {
          path.delete
        } else {
          zunit
        }
    } yield ()
  }

}
