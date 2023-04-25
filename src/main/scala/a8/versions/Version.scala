package a8.versions


import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import a8.versions.Version.BuildInfo
import a8.versions.model.BranchName

import scala.util.Try

import fastparse._, NoWhitespace._

object Version {

  def parse(v: String): Try[Version] = Try {
    fastparse.parse(v, VersionParser.Parser(_)) match {
      case Parsed.Success(v, _) =>
        v
      case f@ Parsed.Failure(_,_,_) =>
        throw new RuntimeException(s"unable to parse version ${v} -- ${f.msg}")
    }
  }

  implicit val orderingByBuildInfo: Ordering[BuildInfo] =
    Ordering.by[BuildInfo,BuildTimestamp](_.buildTimestamp)

  case class BuildInfo(
    buildTimestamp: BuildTimestamp,
    branch: BranchName,
  ) {
    override def toString = s"${buildTimestamp}_${branch.value}"
  }

  implicit val orderingByMajorMinorPathBuildTimestamp: Ordering[Version] =
    Ordering.by[Version,(Int, Int, Int, Option[BuildInfo])](_.tupled)

}

case class Version(
  major: Int,
  minor: Int,
  patch: Int,
  buildInfo: Option[BuildInfo],
) {

  lazy val tupled: (Int, Int, Int, Option[BuildInfo]) = {
    (major, minor, patch, buildInfo)
  }

  override def toString(): String =
    s"${major}.${minor}.${patch}${buildInfo.map("-" + _).getOrElse("")}"

}

