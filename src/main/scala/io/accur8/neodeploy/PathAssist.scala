package io.accur8.neodeploy

import a8.shared.ZFileSystem.{File, Path}
import a8.shared.app.LoggingF
import zio.{Task, ZIO}

import java.nio.file.Files

object PathAssist extends LoggingF {

  def symlink(target: Path, link: File, deleteIfExists: Boolean): Task[Unit] =
    ZIO.attemptBlocking {

      def linkExists() =
        Files.exists(link.asNioPath, java.nio.file.LinkOption.NOFOLLOW_LINKS)

      if (deleteIfExists && linkExists() ) {
        logger.debug(s"deleting ${link}")
        Files.delete(link.asNioPath)
      }
      if ( !linkExists()) {
        logger.debug(s"Files.createSymbolicLink(${link.asNioPath}, ${target.asNioPath})")
        Files.createSymbolicLink(link.asNioPath, target.asNioPath): @scala.annotation.nowarn
      }
    }

}
