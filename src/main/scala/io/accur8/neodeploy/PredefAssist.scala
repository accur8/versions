package io.accur8.neodeploy

import a8.shared.ZFileSystem
import a8.shared.ZString
import a8.shared.ZString.ZStringer
import a8.shared.app.BootstrapConfig.TempDir
import a8.shared.app.{LoggerF, LoggingF}
import org.typelevel.ci.CIString
import zio.{Trace, URIO, ZIO}

import java.time.LocalDateTime

object PredefAssist extends LoggingF {

//  implicit val fileZStringer: ZStringer[FileSystem.File] =
//    new ZStringer[FileSystem.File] {
//      override def toZString(a: FileSystem.File): ZString = a.asNioPath.toFile.getAbsolutePath
//    }
//
//  implicit val directoryZStringer: ZStringer[FileSystem.Directory] =
//    new ZStringer[FileSystem.Directory] {
//      override def toZString(a: FileSystem.Directory): ZString = a.asNioPath.toFile.getAbsolutePath
//    }
//
//  implicit val pathZStringer: ZStringer[FileSystem.Path] =
//    new ZStringer[FileSystem.Path] {
//      override def toZString(a: FileSystem.Path): ZString = a.asNioPath.toFile.getAbsolutePath
//    }


  implicit class TaskOps[R, A](effect: zio.ZIO[R, Throwable, A]) {
    /**
      * Will log and swallow any errors
      */
    def logAndPassThroughErrors(context: String)(implicit loggerF: LoggerF, trace: Trace): zio.ZIO[R, Throwable, A] =
      effect
        .onError(th =>
          loggerF.warn(s"${context} failed, logging and re-throwing", th)
        )

  }

  def traceLog[R,E,A](context: String, effect: ZIO[R,E,A])(implicit loggerF: LoggerF, trace: Trace): ZIO[R,E,A] =
    loggerF.trace(s"start ${context}")
      .flatMap(_ => effect)
      .flatMap { v =>
        loggerF.trace(s"success ${context} -- ${v}")
          .as(v)
      }
      .onError { cause =>
        loggerF.trace(s"error ${context}", cause)
      }

  import org.typelevel.ci.CIString

  given CanEqual[CIString, CIString] = CanEqual.derived
  given CanEqual[java.nio.file.attribute.PosixFileAttributes, java.nio.file.attribute.PosixFileAttributes] = CanEqual.derived
  given CanEqual[coursier.Organization, coursier.Organization] = CanEqual.derived
  given CanEqual[coursier.ModuleName, coursier.ModuleName] = CanEqual.derived


  def workDirectoryZ[R,A]: zio.ZIO[R & TempDir & zio.Scope,Throwable,ZFileSystem.Directory] = {

    import a8.shared.SharedImports._

    val date = LocalDateTime.now()
    val uuid: String = java.util.UUID.randomUUID().toString.replace("-", "").take(20)
    val subPath = f"${date.getYear}/${date.getMonthValue}%02d/${date.getDayOfMonth}%02d/${uuid}"

    val acquire: ZIO[TempDir, Throwable, ZFileSystem.Directory] =
      for {
        tempDir <- zservice[TempDir]
      } yield ZFileSystem.dir(tempDir.unresolved.subdir(subPath).absolutePath)

//    val release: ZFileSystem.Directory => URIO[Any, Unit] = { (f: ZFileSystem.Directory) => f.deleteIfExists.logVoid}
    val release: ZFileSystem.Directory => URIO[Any, Unit] = { _ => zunit }

    zio.ZIO.acquireRelease(acquire)(release)

  }


}
