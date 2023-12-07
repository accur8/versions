package io.accur8.neodeploy


import io.accur8.neodeploy.Deployable.{InfraStructureDeployable, ServerDeployable, UserDeployable, allDeployables}
import io.accur8.neodeploy.model.{ApplicationName, DomainName, ServerName, UserLogin, Version}
import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedRepository, ResolvedServer, ResolvedUser}
import org.rogach.scallop.{ArgType, ValueConverter}
import a8.shared.SharedImports.*
import a8.shared.StringValue
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
}

case class RawDeployArgs(originalValues: List[String], parsedValues: List[ParsedDeployArg]) {
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
          case ParsedDeployArg(name, None, None, _) =>
            resolveApp(name, None)
          case ParsedDeployArg(name, None, Some(v), _) =>
            resolveApp(name, Some(Version(v)))
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
  def parse(value: String): ParsedDeployArg = {
    (value.indexOf(":"), value.indexOf("@")) match {
      case (-1, -1) =>
        ParsedDeployArg(value, None, None, value)
      case (-1,i) =>
        val name = value.substring(0, i)
        val server = value.substring(i + 1)
        ParsedDeployArg(name, None, Some(server), value)
      case (i, -1) =>
        val name = value.substring(0, i)
        val user = value.substring(i + 1)
        ParsedDeployArg(name, Some(user), None, value)
      case (i, j) =>
        val name = value.substring(0, i)
        val user = value.substring(i + 1, j)
        val server = value.substring(j + 1)
        ParsedDeployArg(name, Some(user), Some(server), value)
    }
  }
}
case class ParsedDeployArg(name: String, userPart: Option[String], serverPart: Option[String], originalValue: String)

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
  override def systemState(deployUser: DeployUser): M[SystemState] = infraDeployable.systemState(deployUser)
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