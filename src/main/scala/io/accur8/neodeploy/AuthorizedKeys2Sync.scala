package io.accur8.neodeploy


import a8.shared.ZFileSystem.file
import zio.Task
import a8.shared.SharedImports._
import a8.shared.app.Logging
import a8.shared.{FileSystem, ZFileSystem, ZString}
import com.softwaremill.sttp.Uri
import io.accur8.neodeploy.systemstate.SystemState.Directory
import io.accur8.neodeploy.model.{AuthorizedKey, UserDescriptor}
import io.accur8.neodeploy.resolvedmodel.ResolvedUser
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemStateModel._

object AuthorizedKeys2Sync extends Sync[ResolvedUser] with Logging {

  override val name: Sync.SyncName = Sync.SyncName("authorized_keys2")

  def configFile(input: ResolvedUser): ZFileSystem.File =
    input.home.subdir(".ssh").file("authorized_keys2")

  def contents(input: ResolvedUser): Task[String] =
    input
      .resolvedAuthorizedKeys(Set.empty)
      .map(
        _.flatMap(_.lines)
          .mkString("\n")
      )


  override def systemState(user: ResolvedUser): M[SystemState] =
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

