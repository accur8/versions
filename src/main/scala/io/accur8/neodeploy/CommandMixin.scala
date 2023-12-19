package io.accur8.neodeploy


import SharedImports.{zsucceed, *}
import a8.shared.ZFileSystem
import io.accur8.neodeploy.CommandMixin.CommandException
import io.accur8.neodeploy.systemstate.SystemStateModel.Command
import zio.{Chunk, Trace, UIO, ZIO}
import zio.process.CommandError
import zio.process.CommandError.NonZeroErrorCode

object CommandMixin {
  case class CommandException(cause: CommandError, command: Command) extends Exception
}

trait CommandMixin extends LoggingF { self: systemstate.SystemStateModel.Command =>

  def appendArgsSeq(args: Iterable[String]): Command =
    Command(args = this.args ++ args)

  def asZioCommand: zio.process.Command =
    zio.process.Command(args.head, args.tail.toSeq :_*)

  def exec(
    logLinesEffect: Chunk[String]=>UIO[Unit] = _ => zunit
  )(implicit trace: Trace): N[Command.Result] = {
    def impl(wd: ZFileSystem.Directory) = {
      asZioCommand
        .workingDirectory(wd.asNioPath.toFile)
        .redirectErrorStream(true)
        .run
        .flatMap { process =>
          process
            .stdout
            .linesStream
            .mapChunksZIO { lines =>
              logLinesEffect(lines)
                .as(lines)
            }
            .runCollect
            .flatMap { lines =>
              process
                .exitCode
                .map(_ -> lines)
            }
        }
        .either
        .flatMap {
          case Left(ce) =>
            ZIO.fail(CommandException(ce, this))
          case Right((exitCode, lines)) =>
            if (failOnNonZeroExitCode && exitCode.code > 0) {
              val output = lines.map("    " + _).mkString("\n")
              loggerF.warn(s"command failed with exit code ${exitCode.code} -- ${args.mkString(" ")} -- \n${output}") *>
                ZIO.fail(CommandException(NonZeroErrorCode(exitCode), this))
            } else if (exitCode.code > 0) {
              val output = lines.map("    " + _).mkString("\n")
              loggerF.debug(s"command had non-zero exit code ${exitCode.code} -- ${args.mkString(" ")} -- \n${output}") *>
                zsucceed(Command.Result(exitCode, lines))
            } else {
              zsucceed(Command.Result(exitCode, lines))
            }
        }
    }

    for {
      wd <- workingDirectory.map(_.zdir).getOrElse(zsucceed(ZFileSystem.dir(".")))
      _ <- loggerF.info(s"running in ${wd} -- ${args.mkString(" ")}")
      result <- impl(wd)
    } yield result

  }

  def inDirectory(workingDirectory: VFileSystem.Directory): Command =
    copy(workingDirectory = Some(workingDirectory))

  def execInline: N[Int] =
    exec()
      .map(_.exitCode.code)

  def execCaptureOutput(implicit trace: Trace): N[Command.Result] =
    exec()

  def execLogOutput(implicit trace: Trace, loggerF: LoggerF): N[Command.Result] =
    exec(logLinesEffect = { lines =>
      if (lines.isEmpty) {
        zunit
      } else {
        loggerF.debug(lines.mkString("\n   ", "\n   ", "\n    "))
      }
    })

  def appendArgs(args: String*): Command =
    Command(args = this.args ++ args)

}
