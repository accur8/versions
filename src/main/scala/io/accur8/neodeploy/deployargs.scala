package io.accur8.neodeploy


import io.accur8.neodeploy.Deployable.{AppDeployable, DatabaseDeployable, DnsDeployable, InfraStructureDeployable, PgbackrestClient, PgbackrestServer, RsnapshotServer, ServerDeployable, UserDeployable}
import io.accur8.neodeploy.model.{ApplicationName, DomainName, ServerName, UserLogin, Version, VersionBranch}
import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedRepository, ResolvedServer, ResolvedUser}
import org.rogach.scallop.{ArgType, ValueConverter}
import a8.shared.SharedImports.*
import a8.shared.StringValue
import a8.versions.model.BranchName
import cats.parse.Parser0
import io.accur8.neodeploy.DeployUser.{InfraUser, RegularUser}
import io.accur8.neodeploy.model.VersionBranch.VersionBranchImpl
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemStateModel.M
import zio.Task

case class ResolvedDeployables(parseResults: Iterable[Either[String,Deployable]]) {

  def errorMessages: Option[Iterable[String]] =
    parseResults
      .flatMap(_.swap.toOption)
      .toNonEmpty

  def asIterable: Iterable[Deployable] =
    parseResults
      .flatMap(_.toOption)

  def asLocalDeployArgs: Iterable[String] =
    asIterable
      .flatMap(_.localDeployArgs)

  def asCommandLineArgs: Iterable[String] =
    asIterable
      .map(_.originalArg)

}

object DeployArgParser {

  val parser = new DeployArgParser

  def parse(values: Iterable[String], resolvedRepository: ResolvedRepository): ResolvedDeployables = {
    ResolvedDeployables(values.map(parse(_, resolvedRepository)))
  }

  def parse(value: String, resolvedRepository: ResolvedRepository): Either[String, Deployable] = {
    val suffix = value.splitList(":").reverse.headOption.map(_.toLowerCase)

    def serverParse: Either[String,ServerName] =
      parser
        .serverDeployable
        .parseAll(value)
        .map { case (serverName, appName) =>
          serverName
        }
        .leftMap(e => s"unable to parse ${value} -- ${e}")

    def domainParse: Either[String, DomainName] =
      parser
        .domainDeployable
        .parseAll(value)
        .map { case (serverName, appName) =>
          serverName
        }
        .leftMap(e => s"unable to parse ${value} -- ${e}")

    def userParse: Either[String, (UserLogin,ServerName)] =
      parser
        .userDeployable
        .parseAll(value)
        .map { case (userLogin, serverName, appName) =>
          userLogin -> serverName
        }
        .leftMap(e => s"unable to parse ${value} -- ${e}")

    def installParse: Either[String, (DomainName, VersionBranch)] =
      parser
        .installDeployable
        .parseAll(value)
        .leftMap(e => s"unable to parse ${value} -- ${e}")

    val splitValues = value.splitList(":")

    splitValues.reverse.headOption.map(_.toLowerCase) match {
      case Some("dns") =>
        Right(DnsDeployable)
      case Some("rsnapshot-server") =>
        Right(RsnapshotServer)
      case Some("pgbackrest-server") =>
        Right(PgbackrestServer)
      case Some("pgbackrest-client") =>
        serverParse
          .map(PgbackrestClient(_))
      case Some("caddy") =>
        serverParse
          .map(Deployable.CaddyDeployable(_))
      case Some("database") =>
        domainParse
          .map(DatabaseDeployable(_))
      case Some("install") =>
        installParse
          .map(t => AppDeployable(t._1, resolvedRepository.applicationByDomainName.get(t._1), t._2))
      case Some(v) if splitValues.length == 1 =>
        val domainName = DomainName(v)
        Right(AppDeployable(domainName, resolvedRepository.applicationByDomainName.get(domainName), VersionBranch.Empty))
      case Some(_) if splitValues.length == 2 =>
        val domainName = DomainName(splitValues(0))
        Right(AppDeployable(domainName, resolvedRepository.applicationByDomainName.get(domainName), VersionBranchImpl(Version(splitValues(1)), None)))
      case v =>
        Left(s"unable to parse ${value} - don't know how to handle ${v}")
    }

  }

}

class DeployArgParser {

  import cats.parse.Parser
  import cats.parse.Rfc5234.{alpha, digit}

  lazy val specialChars = Set('-', '_', '.')
  lazy val special: Parser[Char] = Parser.charWhere(specialChars)
  lazy val at: Parser[Unit] = Parser.char('@')
  lazy val colon: Parser[Unit] = Parser.char(':')

  lazy val segment: Parser[String] = (alpha | digit | special).rep.string

  lazy val version =
    (Parser.string("latest") | Parser.string("current") | (digit ~ segment))
      .string
      .map(Version(_))

  lazy val branch =
    segment
      .filter(_ != "install")
      .map(BranchName(_))

  lazy val serverName =
    segment
      .map(ServerName(_))

  lazy val domainName =
    segment
      .map(DomainName(_))

  lazy val userLogin =
    segment
      .map(UserLogin(_))

  lazy val userPart = segment <* at
  lazy val serverPart = segment <* colon

  lazy val serverDeployable: Parser[(ServerName, String)] =
    (serverName <* colon) ~ segment

  lazy val userDeployable: Parser[(UserLogin, ServerName, String)] =
    ((userLogin <* at) ~ (serverName <* colon) ~ segment)
      .map {
        case ((userLogin, serverName), appName) =>
          (userLogin, serverName, appName)
      }

  lazy val versionBranchImpl: Parser[VersionBranchImpl] =
    (version ~ (colon *> branch).backtrack.?).map {
      case (version, branchOpt) =>
        VersionBranchImpl(version, branchOpt)
    }

  lazy val installDeployable: Parser[(DomainName, VersionBranch)] =
    installDeployable1 //| installDeployable2

  lazy val installSuffix: Parser[VersionBranch] = (
    (colon *> versionBranchImpl <* Parser.string(":install")).backtrack
    | Parser.string(":install").map(_ => VersionBranch.Empty)
  )

  lazy val installDeployable1: Parser[(DomainName, VersionBranch)] =
    (domainName ~ installSuffix.?)
      .map { t =>
        (t._1, t._2.getOrElse(VersionBranch.Empty))
      }

  lazy val installDeployable2: Parser[(DomainName, VersionBranch)] =
    domainName
      .map(_ -> VersionBranch.Empty)

  lazy val domainDeployable: Parser[(DomainName, String)] =
    (domainName <* colon) ~ segment

}

case class DeployResult(
  deployUser: DeployUser,
  appVersions: Iterable[(ApplicationName, Version)] = Nil,
)

object DeployUser {
  given CanEqual[DeployUser,DeployUser] = CanEqual.derived
  case object InfraUser extends DeployUser
  case class RegularUser(user: ResolvedUser) extends DeployUser
}

sealed trait DeployUser {

  def resolvedUserOnly: M[ResolvedUser] =
    this match {
      case InfraUser =>
        zfail(new RuntimeException("expected a regular user and not an infra user"))
      case RegularUser(ru) =>
        zsucceed(ru)
    }

  def withResolvedUser[A](fn: ResolvedUser => A): A =
    this match {
      case DeployUser.InfraUser =>
        sys.error("excepted a regular user and not an infra user")
      case DeployUser.RegularUser(user) =>
        fn(user)
    }

  def withResolvedUserZ[A](fn: ResolvedUser => A): Task[A] =
    this match {
      case DeployUser.InfraUser =>
        zfail(
          new RuntimeException("excepted a regular user and not an infra user")
        )
      case DeployUser.RegularUser(user) =>
        zblock(fn(user))
    }
}


object DeployId extends StringValue.Companion[DeployId]
case class DeployId(value: String) extends StringValue
