package a8.versions

import a8.versions.model.BranchName

import java.time.{LocalDate, LocalDateTime, LocalTime, Month}

object VersionParser {

  import fastparse._, NoWhitespace._

  def Parser[$: P] = P(
    Digits ~ "." ~ Digits ~ "." ~ Digits ~ BuildInfo.? ~ End
  ).map { case (major, minor, patch, buildInfo) =>
      ParsedVersion(
        major,
        minor,
        patch,
        buildInfo,
      )
  }

  def BuildInfo[$: P] =
    P("-" ~ BuildTimestampP ~ "_" ~ Branch)
      .map { case (ts, br) => ParsedVersion.BuildInfo(ts, BranchName(br.trim)) }

  def BuildTimestampP[$: P]: P[BuildTimestamp] = P(
    BuildDate ~ "_" ~ BuildTime
  ).map { case (year, month, day, (hour, minute, second)) =>
    BuildTimestamp(year, month, day, hour, minute, second)
  }

  def BuildDate[$: P] = P(
    Digit4 ~ Digit2 ~ Digit2
  )

  def BuildTime[$: P] = P(
    Digit2 ~ Digit2 ~ Digit2.?
  )

  def Branch[$: P] = P(CharsWhile(_.isLetterOrDigit).!)

  def Digits[$: P] = P(Digit.rep(min=1).!.map(_.toInt))

  def Digit2[$: P] = P(Digit.rep(min=2,max=2)).!.map(_.toInt)
  def Digit4[$: P] = P(Digit.rep(min=4,max=4)).!.map(_.toInt)

  def Digit[$: P] = P(CharIn("0123456789"))

}
