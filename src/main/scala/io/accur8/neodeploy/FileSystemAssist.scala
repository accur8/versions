package io.accur8.neodeploy


import a8.shared.ZFileSystem
import java.nio.file.Paths
import java.nio.file.Files
import a8.shared.SharedImports._
import zio.ZIO
import a8.common.logging.LoggingF
import PredefAssist._

object FileSystemAssist extends LoggingF {

  case class FileSet(root: ZFileSystem.Directory, paths: Vector[ZFileSystem.Path] = Vector.empty) {
    private val rootPathStr = root.asNioPath.toFile().getAbsolutePath()
    def addPath(pathStr: String, mustExist: Boolean = true): FileSet = {
      val nioPath: java.nio.file.Path = Paths.get(rootPathStr, pathStr)
      val pathAsFile = nioPath.toFile()
      val absolutePathStr = pathAsFile.getAbsolutePath()
      val resolvedPathOpt: Option[ZFileSystem.Path] =
        if ( pathAsFile.isDirectory() ) {
          ZFileSystem.dir(absolutePathStr).some
        } else if ( pathAsFile.isFile() ) {
          ZFileSystem.file(absolutePathStr).some
        } else if ( pathAsFile.exists() ) {
          sys.error(s"path ${absolutePathStr} is neither a file or directory")
        } else if ( mustExist ) {
          sys.error(s"path ${absolutePathStr} does not exist")
        } else {
          None
        }
      
      resolvedPathOpt match {
        case Some(resolvedPath) =>
          // validate this path is child of the root
          resolvedPath.relativeTo(root): @scala.annotation.nowarn
          copy(paths = paths :+ resolvedPath)
        case None => 
          this
      }
    }

    def copyTo(target: ZFileSystem.Directory): zio.Task[Unit] =
      paths
        .map {
          case f: ZFileSystem.File =>
            val targetFile = target.file(f.relativeTo(root))
            targetFile.parent.resolve: @scala.annotation.nowarn
            for {
              _ <- loggerF.debug(z"copying file ${f} --> ${targetFile}")
              _ <- f.copyTo(targetFile)
            } yield ()
          case d: ZFileSystem.Directory =>
            val targetDir = target.subdir(d.relativeTo(root)).parentOpt.get
            for {
              _ <- targetDir.resolve
              _ <- loggerF.debug(z"copying directory ${d} --> ${targetDir}")
              _ <- d.copyTo(targetDir)
            } yield ()
        }
        .sequence
        .as(())

  }

}
