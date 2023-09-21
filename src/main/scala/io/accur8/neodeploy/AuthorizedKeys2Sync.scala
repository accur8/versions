package io.accur8.neodeploy


import VFileSystem.File
import zio.Task
import SharedImports._
import a8.common.logging.Logging
import a8.shared.{FileSystem, ZFileSystem, ZString}
import sttp.model.Uri
import io.accur8.neodeploy.systemstate.SystemState.Directory
import io.accur8.neodeploy.model.{AuthorizedKey, UserDescriptor}
import io.accur8.neodeploy.resolvedmodel.ResolvedUser
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemStateModel._

object AuthorizedKeys2Sync extends Logging {

  def configFile(input: ResolvedUser): VFileSystem.File =
    input.home.subdir(".ssh").file("authorized_keys2")

  def contents(input: ResolvedUser): N[String] =
    input
      .resolvedAuthorizedKeys(Set.empty)
      .map(
        _.flatMap(_.lines)
          .mkString("\n")
      )


  def systemState(user: ResolvedUser): M[SystemState] =
    contents(user)
      .map { fileContents =>
        val file = configFile(user)
        SystemState.Composite(
          "authorized keys 2",
          Vector(
            SystemState.Directory(file.parent, UnixPerms("0700")),
            SystemState.TextFile(file, fileContents, UnixPerms("0644"))
          )
        )
      }

}

