package a8.versions


import a8.shared.FileSystem.Directory
import a8.common.logging.Logging

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintWriter}
import scala.sys.process

object Exec {

  def apply(args: String*): Exec =
    Exec(args, None)

  case class Result(
    exitCode: Int,
    stdout: String,
    stderr: String
  )

}

case class Exec(
  args: Iterable[String],
  workingDirectory: Option[Directory] = None
)
  extends Logging
{

  def inDirectory(directory: Directory) =
    copy(workingDirectory = Some(directory))

  import Exec._

  private def _process: process.ProcessBuilder =
    sys.process.Process(args.toSeq, workingDirectory.map(d => new java.io.File(d.canonicalPath))) #< new ByteArrayInputStream(Array())

  def execCaptureOutput(failOnNonZeroExitCode: Boolean = true, emptyStdin: Boolean = true): Result = {
    import sys.process._
    val stdout = new ByteArrayOutputStream
    val stderr = new ByteArrayOutputStream
    val stdoutWriter = new PrintWriter(stdout)
    val stderrWriter = new PrintWriter(stderr)
    logger.debug(toString)
    val exitCode = _process.!(ProcessLogger(stdoutWriter.println, stderrWriter.println))
    stdoutWriter.close()
    stderrWriter.close()
    val result = Result(
      exitCode = exitCode,
      stdout = stdout.toString,
      stderr = stderr.toString
    )
    if ( failOnNonZeroExitCode && exitCode != 0 )
      sys.error(s"error \n    ${this}\n    ${result}")
    result
  }

  /**
    * will output to stdout and stderr
    */
  def execInline(failOnNonZeroExitCode: Boolean = true): Int = {
    logger.info(toString)
    val exitCode = _process.!
    if ( failOnNonZeroExitCode && exitCode != 0 )
      sys.error(s"error ${this} exitCode = ${exitCode}")
    exitCode
  }

  lazy val argsAsString =
    args
      .map { arg =>
        if ( arg.exists(_.isWhitespace) ) s"'${arg}'"
        else arg
      }
      .mkString(" ")

  override def toString =
    s"running${workingDirectory.map(d=>s" with a cwd of ${d}").getOrElse("")} the command -- ${argsAsString}"

}
