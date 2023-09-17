package io.accur8.neodeploy


import io.accur8.neodeploy.DeploySubCommand.*
import io.accur8.neodeploy.model.{CaddyDirectory, ServerName, UserLogin}
import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedRepository, ResolvedServer, ResolvedUser}
import a8.shared.SharedImports.*
import io.accur8.neodeploy.DeployUser.RegularUser
import io.accur8.neodeploy.plugin.DnsPlugin
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemStateModel.M

import scala.util.Left

object Deployable {

  case object PgbackrestServer extends InfraStructureDeployable {
    override val name: String = "pgbackrest-server"
    override def deployUsers(resolvedRepo: ResolvedRepository): Iterable[DeployUser] =
      resolvedRepo
        .servers
        .flatMap(_.resolvedUsers)
        .filter(_.plugins.pgbackrestServerOpt.isDefined)
        .map(RegularUser(_))


    override def systemState(deployUser: DeployUser): M[SystemState] =
      deployUser
        .resolvedUserOnly
        .flatMap(resolvedUser =>
          resolvedUser
            .plugins
            .pgbackrestServerOpt
            .map(_.systemState(resolvedUser))
            .getOrElse(zfail(new RuntimeException("expected a pgbackrest server plugin")))
        )

  }

  case object PgbackrestClient extends ServerDeployable {
    override val name: String = "pgbackrest-client"
    override def deployUsers(resolvedServer: ResolvedServer): Iterable[DeployUser] =
      resolvedServer
        .fetchUserOpt(UserLogin("postgres"))
        .map(RegularUser(_))


    override def systemState(resolvedUser: ResolvedUser): M[SystemState] =
      resolvedUser
        .plugins
        .pgbackrestClientOpt
        .map(_.systemState(resolvedUser))
        .getOrElse(zfail(new RuntimeException("expected a pgbackrest client plugin")))

  }

  case object RsnapshotServer extends InfraStructureDeployable {
    override val name: String = "rsnapshot-server"
    override def deployUsers(resolvedRepo: ResolvedRepository): Iterable[DeployUser] =
      resolvedRepo
        .servers
        .flatMap(_.resolvedUsers)
        .filter(_.plugins.resolvedRSnapshotServerOpt.isDefined)
        .map(RegularUser(_))

    override def systemState(deployUser: DeployUser): M[SystemState] =
      deployUser
        .resolvedUserOnly
        .map(_.plugins.resolvedRSnapshotServerOpt)
        .flatMap {
          case Some(rsnapshotServer) =>
            zsucceed(rsnapshotServer.systemState)
          case None =>
            zfail(new RuntimeException("expected a rsnapshot server plugin"))
        }
  }

  case object Caddy extends ServerDeployable {
    override val name: String = "caddy"
    override def deployUsers(resolvedServer: ResolvedServer): Iterable[DeployUser] =
      resolvedServer
        .fetchUserOpt(UserLogin("root"))
        .map(RegularUser(_))

    override def systemState(resolvedUser: ResolvedUser): M[SystemState] =
      CaddySync.systemState(resolvedUser.server)

  }

  case object Supervisor extends ServerDeployable {
    override val name: String = "supervisor"
    override def deployUsers(resolvedServer: ResolvedServer): Iterable[DeployUser] =
      resolvedServer
        .fetchUserOpt(UserLogin("root"))
        .map(RegularUser(_))

    override def systemState(resolvedUser: ResolvedUser): M[SystemState] =
      ???
  }
  case object AuthorizedKeys extends UserDeployable {
    override val name: String = "AuthorizedKeys"
    override def systemState(user: ResolvedUser): M[SystemState] =
      AuthorizedKeys2Sync.systemState(user)
  }
  case object ManagedKeys extends UserDeployable {
    override val name: String = "ManagedKeys"
    override def systemState(user: ResolvedUser): M[SystemState] =
      ManagedSshKeysSync.systemState(user)
  }
  case object DnsDeployable extends InfraStructureDeployable {
    override val name: String = "dns"
    override def deployUsers(resolvedRepo: ResolvedRepository): Iterable[DeployUser] =
      Iterable(DeployUser.InfraUser(resolvedRepo))
    override def systemState(deployUser: DeployUser): M[SystemState] =
      DnsPlugin.systemState(deployUser.resolvedRepo)
  }

  sealed trait UserDeployable extends Deployable {

    def systemState(user: ResolvedUser): M[SystemState]

    override def parseNameMatchedDeployArg(parsedDeployArg: ParsedDeployArg, resolvedRepo: ResolvedRepository): Either[String, DeployArg] = {
      parsedDeployArg match {
        case ParsedDeployArg(name, Some(ul), Some(sn), _) =>
          val user = UserLogin(ul)
          val server = ServerName(sn)
          val resolvedUserOpt =
            resolvedRepo
              .serverOpt(server)
              .flatMap(_.fetchUserOpt(user))
          resolvedUserOpt match {
            case None =>
              Left(s"User $user not found on server $server")
            case Some(ru) =>
              Right(UserDeploy(ru, this, parsedDeployArg))
          }
        case pda: ParsedDeployArg =>
          Left(s"Invalid arguments for $name: $pda")
      }
    }
  }

  sealed trait ServerDeployable extends Deployable {

    def systemState(resolvedUser: ResolvedUser): M[SystemState]

    def deployUsers(resolvedServer: ResolvedServer): Iterable[DeployUser]

    override def parseNameMatchedDeployArg(parsedDeployArg: ParsedDeployArg, resolvedRepo: ResolvedRepository): Either[String, DeployArg] = {
      parsedDeployArg match {
        case ParsedDeployArg(name, None, Some(sn), _) =>
          val server = ServerName(sn)
          resolvedRepo.serverOpt(server) match {
            case Some(rs) =>
              Right(ServerDeploy(rs, this, parsedDeployArg))
            case None =>
              Left(s"Server $server not found")
          }
        case pda: ParsedDeployArg =>
          Left(s"Invalid arguments for $name: $pda")
      }
    }
  }


  sealed trait InfraStructureDeployable extends Deployable {

    def systemState(deployUser: DeployUser): M[SystemState]

    def deployUsers(resolvedRepo: ResolvedRepository): Iterable[DeployUser]

    override def parseNameMatchedDeployArg(parsedDeployArg: ParsedDeployArg, resolvedRepo: ResolvedRepository): Either[String, DeployArg] = {
      parsedDeployArg match {
        case ParsedDeployArg(name, None, None, _) =>
          Right(InfraDeploy(resolvedRepo, this, parsedDeployArg))
        case pda: ParsedDeployArg =>
          Left(s"Invalid arguments for $name: $pda")
      }
    }
  }

  lazy val userDeployables = List(AuthorizedKeys, ManagedKeys)
  lazy val serverDeployables = List(PgbackrestClient, Caddy, Supervisor)
  lazy val infraStructureDeployables = List(PgbackrestServer, RsnapshotServer, DnsDeployable)

  lazy val allDeployables: Seq[Deployable] = userDeployables ++ serverDeployables ++ infraStructureDeployables


}


sealed trait Deployable {

  val name: String
  lazy val nameLc = name.toLowerCase

  def parseNameMatchedDeployArg(parsedDeployArg: ParsedDeployArg, resolvedRepo: ResolvedRepository): Either[String, DeployArg]

  def resolveDeployArg(parsedDeployArg: ParsedDeployArg, resolvedRepo: ResolvedRepository): Option[Either[String, DeployArg]] = {
    (parsedDeployArg.name =:= name)
      .toOption {
        parseNameMatchedDeployArg(parsedDeployArg, resolvedRepo)
      }
  }
}
