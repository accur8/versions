package io.accur8.neodeploy


import a8.shared.json.{JsonCodec, JsonTypedCodec}
import a8.shared.json.ast.{JsArr, JsStr}
import zio.{Chunk, ExitCode, Trace, UIO, ZIO}
import a8.shared.SharedImports._
import PredefAssist._
import a8.shared.CompanionGen
import a8.shared.ZFileSystem.Directory
import a8.shared.app.{LoggerF, LoggingF}
import io.accur8.neodeploy.Command.CommandException
import io.accur8.neodeploy.MxCommand.MxCommand
import io.accur8.neodeploy.systemstate.SystemState.RunCommandState
import zio.process.CommandError
import zio.process.CommandError.NonZeroErrorCode

object Command {

  implicit val jsonCodec: JsonCodec[Command] = {
    val delegate = new MxCommand {}

    val jsobjCodec = delegate.jsonCodec.asJsonCodec

    val jsarrCodec =
      JsonTypedCodec.JsArr.dimap[Command](
        arr => Command(arr.values.collect{ case JsStr(s) => s }),
        cmd => JsArr(cmd.args.map(JsStr.apply).toList)
      ).asJsonCodec

    JsonCodec.or(jsobjCodec, jsarrCodec)

  }

  def apply(args: String*): Command =
    new Command(args)

  case class Result(
    exitCode: ExitCode,
    outputLines: Chunk[String],
  )

  case class CommandException(cause: CommandError, command: Command) extends Exception

}

@CompanionGen
case class Command(args: Iterable[String], workingDirectory: Option[Directory] = None) extends LoggingF {

  def asSystemStateCommand =
    systemstate.SystemStateModel.Command(
      args = args,
      workingDirectory = workingDirectory,
    )

  def workingDirectory(wd: Directory): Command =
    copy(workingDirectory = wd.some)

  def appendArgs(args: String*): Command =
    Command(args = this.args ++ args)

  def appendArgsSeq(args: Iterable[String]): Command =
    Command(args = this.args ++ args)

  def asZioCommand: zio.process.Command =
    zio.process.Command(args.head, args.tail.toSeq :_*)

  def execDropOutput: ZIO[Any, CommandException, Unit] =
    exec()
      .as(())

  def execCaptureOutput: ZIO[Any, CommandException, Command.Result] =
    exec()

  def execLogOutput(implicit trace: Trace, loggerF: LoggerF): ZIO[Any, CommandException, Command.Result] =
    exec(logLinesEffect = { lines =>
      if (lines.isEmpty) {
        zunit
      } else {
        loggerF.debug(lines.mkString("\n   ", "\n   ", "\n    "))
      }
    })

  def exec(
    failOnNonZeroExitCode: Boolean = true,
    logLinesEffect: Chunk[String]=>UIO[Unit] = _ => zunit
  )(implicit trace: Trace): ZIO[Any, CommandException, Command.Result] = {
    val wd = workingDirectory.map(_.asNioPath.toFile).getOrElse(new java.io.File(".")).getAbsoluteFile
    loggerF.info(s"running in ${wd} -- ${args.mkString(" ")}") *>
    asZioCommand
      .workingDirectory(workingDirectory.map(_.asNioPath.toFile).getOrElse(new java.io.File(".")))
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
          } else
            zsucceed(Command.Result(exitCode, lines))
      }

  }

}
