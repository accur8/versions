package a8.versions


import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import a8.versions.ParsedVersion.BuildInfo
import a8.versions.model.BranchName

import scala.util.Try
import fastparse.*
import NoWhitespace.*
import a8.shared.json.{JsonTypedCodec, ast}
import a8.shared.json.impl.JsonCodecs

object ParsedVersion {

  def parse(v: String): Try[ParsedVersion] = Try {
    fastparse.parse(v, VersionParser.Parser(_)) match {
      case Parsed.Success(v, _) =>
        v
      case f@ Parsed.Failure(_,_,_) =>
        throw new RuntimeException(s"unable to parse version ${v} -- ${f.msg}")
    }
  }

  given CanEqual[ParsedVersion, ParsedVersion] = CanEqual.derived

  implicit val orderingByBuildInfo: Ordering[BuildInfo] =
    Ordering.by[BuildInfo,BuildTimestamp](_.buildTimestamp)

  case class BuildInfo(
    buildTimestamp: BuildTimestamp,
    branch: BranchName,
  ) {
    override def toString = s"${buildTimestamp}_${branch.value}"
  }

  given jsonCodec: JsonTypedCodec[ParsedVersion, ast.JsStr] =
    JsonTypedCodec.string.dimap[ParsedVersion](
      s => parse(s).get,
      _.toString(),
    )

  implicit val orderingByMajorMinorPathBuildTimestamp: Ordering[ParsedVersion] =
    Ordering.by[ParsedVersion,(Int, Int, Int, Option[BuildInfo])](_.tupled)

}

case class ParsedVersion(
  major: Int,
  minor: Int,
  patch: Int,
  buildInfo: Option[BuildInfo],
) {

  lazy val tupled: (Int, Int, Int, Option[BuildInfo]) = {
    (major, minor, patch, buildInfo)
  }

  lazy val asNeodeployVersion = io.accur8.neodeploy.model.Version(toString())

  override def toString(): String =
    s"${major}.${minor}.${patch}${buildInfo.map("-" + _).getOrElse("")}"

}

