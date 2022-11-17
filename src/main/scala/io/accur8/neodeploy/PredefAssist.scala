package io.accur8.neodeploy

import a8.shared.FileSystem
import a8.shared.ZString
import a8.shared.ZString.ZStringer
import a8.shared.app.LoggerF
import zio.Trace

object PredefAssist {

  implicit val fileZStringer: ZStringer[FileSystem.File] =
    new ZStringer[FileSystem.File] {
      override def toZString(a: FileSystem.File): ZString = a.asNioPath.toFile.getAbsolutePath
    }

  implicit val directoryZStringer: ZStringer[FileSystem.Directory] =
    new ZStringer[FileSystem.Directory] {
      override def toZString(a: FileSystem.Directory): ZString = a.asNioPath.toFile.getAbsolutePath
    }

  implicit val pathZStringer: ZStringer[FileSystem.Path] =
    new ZStringer[FileSystem.Path] {
      override def toZString(a: FileSystem.Path): ZString = a.asNioPath.toFile.getAbsolutePath
    }


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


}
