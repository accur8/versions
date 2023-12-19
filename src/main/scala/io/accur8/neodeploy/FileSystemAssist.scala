package io.accur8.neodeploy


import java.nio.file.Paths
import java.nio.file.Files
import SharedImports._
import zio.ZIO
import a8.common.logging.LoggingF
import PredefAssist._
import io.accur8.neodeploy.VFileSystem

object FileSystemAssist extends LoggingF {

  case class FileSet(root: VFileSystem.Directory, paths: Vector[(VFileSystem.File|VFileSystem.Directory, Boolean)] = Vector.empty) {

    private val rootPathStr = root.path

    def addDirectory(pathStr: String, mustExist: Boolean = true): FileSet =
      copy(paths = paths :+ (VFileSystem.dir(pathStr) -> mustExist))

    def addFile(pathStr: String, mustExist: Boolean = true): FileSet =
      copy(paths = paths :+ (VFileSystem.file(pathStr) -> mustExist))

//      val nioPath: java.nio.file.Path = Paths.get(rootPathStr, pathStr)
//      val pathAsFile = nioPath.toFile()
//      val absolutePathStr = pathAsFile.getAbsolutePath()
//      val resolvedPathOpt: Option[VFileSystem.Path] =
//        if ( pathAsFile.isDirectory() ) {
//          ZFileSystem.dir(absolutePathStr).some
//        } else if ( pathAsFile.isFile() ) {
//          ZFileSystem.file(absolutePathStr).some
//        } else if ( pathAsFile.exists() ) {
//          sys.error(s"path ${absolutePathStr} is neither a file or directory")
//        } else if ( mustExist ) {
//          sys.error(s"path ${absolutePathStr} does not exist")
//        } else {
//          None
//        }
//
//      resolvedPathOpt match {
//        case Some(resolvedPath) =>
//          // validate this path is child of the root
//          resolvedPath.relativeTo(root): @scala.annotation.nowarn
//          copy(paths = paths :+ resolvedPath)
//        case None =>
//          this
//      }
//    }

    def copyTo(target: VFileSystem.Directory): N[Unit] =
      paths
        .map {
          case (f: VFileSystem.File, mustExist) =>
            val relativeSourceFile = f.relativeTo(root)
            val sourceFile = root.file(relativeSourceFile)
            val targetFile = target.file(f.relativeTo(root))
            for {
              _ <- targetFile.parent.resolve
              _ <- loggerF.debug(z"copying file ${sourceFile} --> ${targetFile}")
              _ <- sourceFile.copyTo(targetFile)
            } yield ()
          case (d: VFileSystem.Directory, mustExist) =>
            val relativeSourceDir = d.relativeTo(root)
            val sourceDir = root.subdir(relativeSourceDir)
            val targetDir = target.subdir(relativeSourceDir).parentOpt.get
            for {
              _ <- targetDir.resolve
              _ <- loggerF.debug(z"copying directory ${sourceDir} --> ${targetDir}")
              _ <- sourceDir.copyTo(targetDir)
            } yield ()
        }
        .sequence
        .as(())

  }

}
