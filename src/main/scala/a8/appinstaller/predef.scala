package a8.appinstaller


import a8.common.logging.Logging
import a8.shared.FileSystem

import java.io.{File => JFile}
import language.implicitConversions
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}
import a8.shared.SharedImports._

object predef extends Logging {

  implicit def toJavaIoFile(p: FileSystem.Path): JFile = new JFile(p.canonicalPath)
  implicit def toM3FsFile(f: JFile): FileSystem.File = FileSystem.file(f.getAbsolutePath)
  implicit def toM3FsDir(f: JFile): FileSystem.Directory = FileSystem.dir(f.getAbsolutePath)

  def tryLog[A](context: String)(fn: A)(implicit logger: Logger): Try[A] = {
    try {
      Success(fn)
    } catch {
      case IsNonFatal(th) =>
        logger.warn(context, th)
        Failure(th)
    }
  }

}
