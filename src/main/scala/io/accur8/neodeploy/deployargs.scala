package io.accur8.neodeploy


import io.accur8.neodeploy.Deployable.{InfraStructureDeployable, ServerDeployable, UserDeployable, allDeployables}
import io.accur8.neodeploy.model.{ApplicationName, DomainName, ServerName, UserLogin, Version}
import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedRepository, ResolvedServer, ResolvedUser}
import org.rogach.scallop.{ArgType, ValueConverter}
import a8.shared.SharedImports.*
import a8.shared.StringValue
import cats.parse.Parser0
import io.accur8.neodeploy.DeployUser.{InfraUser, RegularUser}
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemStateModel.M
import zio.Task

object RawDeployArgs {

  implicit val valueConverter: ValueConverter[RawDeployArgs] =
    new ValueConverter[RawDeployArgs] {
      override def parse(s: List[(String, List[String])]): Either[String, Option[RawDeployArgs]] =
        s.find(_._1 == "") match {
          case None =>
            Left("no arguments supplied")
          case Some(args) =>
            Right(Some(RawDeployArgs(args._2, args._2.map(ParsedDeployArg.parse))))
        }

      override val argType: ArgType.V =
        ArgType.LIST
    }

  def apply(originalValues: Iterable[String]): RawDeployArgs =
    RawDeployArgs(originalValues.toList, originalValues.map(ParsedDeployArg.parse))

}

case class RawDeployArgs(originalValues: List[String], rawParsedValues: Iterable[Either[String,ParsedDeployArg]]) {
  lazy val errors = rawParsedValues.collect { case Left(error) => error }
  lazy val parsedValues = rawParsedValues.collect { case Right(parsed) => parsed }
  def resolve(resolvedRepo: ResolvedRepository): Either[String,ResolvedDeployArgs] = {
    val resolvedArgsE = parsedValues.map(DeployArg.resolve(_, resolvedRepo)).toList
    resolvedArgsE.partitionMap(identity) match {
      case (Nil, resolvedArgs) =>
        Right(ResolvedDeployArgs(this, resolvedArgs))
      case (strings, _) =>
        Left(strings.mkString("\n"))
    }
  }
}

case class ResolvedDeployArgs(rawDeployArgs: RawDeployArgs, args: List[DeployArg]) {

  def asCommandLineArgs: Seq[String] =
    rawDeployArgs.originalValues

}

object DeployArg {

  def resolve(parsedValue: ParsedDeployArg, resolvedRepo: ResolvedRepository): Either[String, DeployArg] = {
    val resolvedArgs =
      allDeployables
        .flatMap(_.resolveDeployArg(parsedValue, resolvedRepo))
        .toList

    def resolveApp(appNameStr: String, version: Option[Version]): Either[String, AppDeploy] = {
      val appName = DomainName(appNameStr)
      val apps = resolvedRepo.applications
      apps
        .find(_.isNamed(appName))
        .map(app => AppDeploy(app, version, parsedValue))
        .toRight(s"unable to find app ${appName}")
    }

    resolvedArgs match {
      case Nil =>
        parsedValue match {
          case ParsedDeployArg(name, None, None, version, _) =>
            resolveApp(name, version.map(Version(_)))
          case _ =>
            Left(s"unable to parse app from ${parsedValue.originalValue}")
        }
      case List(Left(errorMsg)) =>
        Left(s"unable to parse ${parsedValue.originalValue} -- ${errorMsg}")
      case List(right) =>
        right
      case l =>
        Left(s"too many results from parsing ${parsedValue.originalValue} -- ${l.mkString}")
    }

  }
}

object ParsedDeployArg {

  object parser {
    import cats.parse.Parser
    import cats.parse.Rfc5234.{alpha, digit}

    lazy val specialChars = Set('-', '_', '.')
    lazy val special: Parser[Char] = Parser.charWhere(specialChars)
    lazy val at: Parser[Unit] = Parser.char('@')
    lazy val colon: Parser[Unit] = Parser.char(':')

    lazy val segment: Parser[String] = (alpha | digit | special).rep.string

    lazy val userPart = segment <* at
    lazy val serverPart = segment <* colon

    lazy val deployArg: Parser0[ParsedDeployArg] =
      deployArg1 | deployArg2

    lazy val deployArg2: Parser0[ParsedDeployArg] = {
      val ppppp: Parser[(String, String)] = ((segment <* colon <* Parser.string("app") <* colon) ~ segment)
      ppppp
        .map { case (n, v) =>
          ParsedDeployArg(n, None, None, Some(v), "")
        }
    }

    lazy val deployArg1: Parser0[ParsedDeployArg] =
      ((userPart.? ~ serverPart).? ~ segment)
        .map { case (t, n) =>
          ParsedDeployArg(n, t.flatMap(_._1), t.map(_._2), None, "")
        }

  }

  def parse(value: String): Either[String, ParsedDeployArg] = {
    parser.deployArg.parseAll(value) match {
      case Left(e) =>
        Left(s"unable to parse ${value} -- ${e}")
      case Right(parsed) =>
        Right(parsed.copy(originalValue = value))
    }
  }

}
case class ParsedDeployArg(name: String, userPart: Option[String], serverPart: Option[String], versionPart: Option[String], originalValue: String)

sealed trait DeployArg {
  def deployUsers: Iterable[DeployUser]
  val originalArg: ParsedDeployArg
  lazy val deployId: DeployId
  def systemState(deployUser: DeployUser): M[SystemState]
}

case class AppDeploy(resolvedApp: ResolvedApp, version: Option[Version], originalArg: ParsedDeployArg) extends DeployArg {
  override lazy val deployId: DeployId = DeployId(z"app-${resolvedApp.name}")
  override def deployUsers: Iterable[DeployUser] = Iterable(RegularUser(resolvedApp.user))
  override def systemState(deployUser: DeployUser): M[SystemState] =
    deployUser
      .resolvedUserOnly
      .flatMap(resolvedUser =>
        ApplicationInstallSync(resolvedUser.appsRootDirectory)
          .systemState(resolvedApp)
      )
}

case class UserDeploy(resolvedUser: ResolvedUser, userDeployable: UserDeployable, originalArg: ParsedDeployArg) extends DeployArg {
  override lazy val deployId: DeployId = DeployId(z"${userDeployable.name}")
  override def deployUsers: Iterable[DeployUser] = Iterable(RegularUser(resolvedUser))
  override def systemState(deployUser: DeployUser): M[SystemState] = userDeployable.systemState(resolvedUser)
}

case class ServerDeploy(resolvedServer: ResolvedServer, serverDeployable: ServerDeployable, originalArg: ParsedDeployArg) extends DeployArg {
  override lazy val deployId: DeployId = DeployId(z"${serverDeployable.name}")
  override def deployUsers: Iterable[DeployUser] = serverDeployable.deployUsers(resolvedServer)
  override def systemState(deployUser: DeployUser): M[SystemState] =
    deployUser
      .resolvedUserOnly
      .flatMap(serverDeployable.systemState)
}

case class InfraDeploy(resolvedRepo: ResolvedRepository, infraDeployable: InfraStructureDeployable, originalArg: ParsedDeployArg) extends DeployArg {
  override lazy val deployId: DeployId = DeployId(z"${infraDeployable.name}")
  override def deployUsers: Iterable[DeployUser] = infraDeployable.deployUsers(resolvedRepo)
  override def systemState(deployUser: DeployUser): M[SystemState] = infraDeployable.systemState(this, deployUser)
}

case class DeployResult(
  deployUser: DeployUser,
  appVersions: Iterable[(ApplicationName, Version)] = Nil,
)


sealed trait DeployUser {

  def resolvedRepo: ResolvedRepository

  def resolvedUserOnly: M[ResolvedUser] =
    this match {
      case InfraUser(_) =>
        zfail(new RuntimeException("expected a regular user and not an infra user"))
      case RegularUser(ru) =>
        zsucceed(ru)
    }

  def withResolvedUser[A](fn: ResolvedUser => A): A =
    this match {
      case DeployUser.InfraUser(_) =>
        sys.error("excepted a regular user and not an infra user")
      case DeployUser.RegularUser(user) =>
        fn(user)
    }

  def withResolvedUserZ[A](fn: ResolvedUser => A): Task[A] =
    this match {
      case DeployUser.InfraUser(_) =>
        zfail(
          new RuntimeException("excepted a regular user and not an infra user")
        )
      case DeployUser.RegularUser(user) =>
        zblock(fn(user))
    }
}
object DeployUser {
  given CanEqual[DeployUser,DeployUser] = CanEqual.derived
  case class InfraUser(resolvedRepo: ResolvedRepository) extends DeployUser
  case class RegularUser(user: ResolvedUser) extends DeployUser {
    override def resolvedRepo: ResolvedRepository = user.server.repository
  }
}

object DeployId extends StringValue.Companion[DeployId]
case class DeployId(value: String) extends StringValue