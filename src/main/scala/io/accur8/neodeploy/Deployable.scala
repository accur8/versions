package io.accur8.neodeploy


import io.accur8.neodeploy.DeploySubCommand.*
import io.accur8.neodeploy.model.{CaddyDirectory, DomainName, ServerName, UserLogin, Version, VersionBranch}
import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedRepository, ResolvedServer, ResolvedUser}
import a8.shared.SharedImports.*
import a8.versions.model.BranchName
import io.accur8.neodeploy.DeployUser.RegularUser
import io.accur8.neodeploy.plugin.DnsPlugin
import io.accur8.neodeploy.systemstate.{DatabasePlugin, SystemState}
import io.accur8.neodeploy.systemstate.SystemStateModel.M

import scala.util.Left

object Deployable {

  case object PgbackrestServer extends InfraStructureDeployable {

    override def localDeployArgs: Iterable[String] = "pgbackrest-server" :: Nil

    override def originalArg: String = "pgbackrest-server"

    override def deployId: DeployId = DeployId("pgbackrest-server")

    override def deployUsers(resolvedRepo: ResolvedRepository): Iterable[DeployUser] =
      resolvedRepo
        .servers
        .flatMap(_.resolvedUsers)
        .filter(_.plugins.pgbackrestServerOpt.isDefined)
        .map(RegularUser(_))

    override def systemState: M[SystemState] =
      ResolvedUser
        .live
        .flatMap(resolvedUser =>
          resolvedUser
            .plugins
            .pgbackrestServerOpt
            .map(_.systemState(resolvedUser))
            .getOrElse(zfail(new RuntimeException("expected a pgbackrest server plugin")))
        )

  }

  case class PgbackrestClient(serverName: ServerName) extends ServerDeployable {

    override def localDeployArgs: Iterable[String] = "pgbackrest-client" :: Nil

    override def originalArg: String = "pgbackrest-client"

    override def deployId: DeployId = DeployId("pgbackrest-client")

    override def deployUsers(resolvedServer: ResolvedServer): Iterable[DeployUser] =
      resolvedServer
        .resolvedUsers
        .filter(_.plugins.pgbackrestClientOpt.isDefined)
        .map(RegularUser(_))

    override def systemState: M[SystemState] =
      ResolvedUser
        .live
        .flatMap(resolvedUser =>
          resolvedUser
            .plugins
            .pgbackrestClientOpt
            .map(_.systemState(resolvedUser))
            .getOrElse(zfail(new RuntimeException("expected a pgbackrest client plugin")))
        )

  }

  case object RsnapshotServer extends InfraStructureDeployable {

    override def localDeployArgs: Iterable[String] = "rsnapshot-server" :: Nil

    override def originalArg: String = "rsnapshot-server"

    override def deployId: DeployId = DeployId("rsnapshot-server")

    override def deployUsers(resolvedRepo: ResolvedRepository): Iterable[DeployUser] =
      resolvedRepo
        .users
        .filter(_.plugins.resolvedRSnapshotServerOpt.isDefined)
        .map(RegularUser(_))

    override def systemState: M[SystemState] =
      ResolvedUser
        .live
        .map(_.plugins.resolvedRSnapshotServerOpt)
        .flatMap {
          case Some(rsnapshotServer) =>
            zsucceed(rsnapshotServer.systemState)
          case None =>
            zfail(new RuntimeException("expected a rsnapshot server plugin"))
        }
  }

  case class CaddyDeployable(serverName: ServerName) extends ServerDeployable {

    override def deployUsers(resolvedServer: ResolvedServer): Iterable[DeployUser] =
      resolvedServer
        .resolvedUsers
        .filter(_.login == UserLogin("root"))
        .map(RegularUser(_))

    override def deployId: DeployId = DeployId("caddy")

    override def localDeployArgs: Iterable[String] = z"caddy" :: Nil

    override def originalArg: String = z"${serverName}:caddy"

    override def systemState: M[SystemState] =
      ResolvedServer
        .live
        .flatMap(rs =>
          CaddySync.systemState(rs)
        )

  }

//  case class Supervisor(serverName: ServerName) extends ServerDeployable {
////    override val name: String = "supervisor"
////    override def deployUsers(resolvedServer: ResolvedServer): Iterable[DeployUser] =
////      resolvedServer
////        .fetchUserOpt(UserLogin("root"))
////        .map(RegularUser(_))
//
//    override def systemState: M[SystemState] =
//      ???
//  }

  case class AuthorizedKeys(userLogin: UserLogin, serverName: ServerName) extends UserDeployable {

    override def deployId: DeployId = DeployId("authorizedkeys")

    override def systemState: M[SystemState] =
      ResolvedUser
        .live
        .flatMap(ru =>
          AuthorizedKeys2Sync.systemState(ru)
        )

  }

  case class ManagedKeys(userLogin: UserLogin, serverName: ServerName) extends UserDeployable {
    override def deployId: DeployId = DeployId("managedkeys")
    override def systemState: M[SystemState] =
      ResolvedUser
        .live
        .flatMap(ru =>
          ManagedSshKeysSync.systemState(ru)
        )
  }

  case object DnsDeployable extends InfraStructureDeployable {

    override def deployId: DeployId = DeployId("dns")

    override def originalArg: String = "dns"

    override def deployUsers(resolvedRepo: ResolvedRepository): Iterable[DeployUser] =
      Iterable(DeployUser.InfraUser)

    override def systemState: M[SystemState] =
      DnsPlugin.systemState

  }

  case class DatabaseDeployable(domainName: DomainName) extends InfraStructureDeployable {

    override def deployId: DeployId = DeployId(z"database-${domainName}")

    override def originalArg: String = z"${domainName}:database"

    override def deployUsers(resolvedRepo: ResolvedRepository): Iterable[DeployUser] =
      Iterable(DeployUser.InfraUser)

    override def systemState: M[SystemState] =
      zservice[ResolvedRepository]
        .map(_.applicationByDomainName.get(domainName))
        .flatMap {
          case Some(app) =>
            DatabasePlugin.systemState(app)
          case None =>
            zfail(new RuntimeException(s"Application $domainName not found"))
        }
  }

  sealed trait UserDeployable extends Deployable {

    val userLogin: UserLogin
    val serverName: ServerName

    override def originalArg: String = z"${userLogin}@${serverName}:${deployId}"

    override def deployUsers(resolvedRepo: ResolvedRepository): Iterable[DeployUser] =
      resolvedRepo
        .userOpt(userLogin, serverName)
        .map(RegularUser(_))

  }

  sealed trait ServerDeployable extends Deployable {

    val serverName: ServerName

    override def deployUsers(resolvedRepo: ResolvedRepository): Iterable[DeployUser] =
      resolvedRepo
        .serverOpt(serverName)
        .toSeq
        .flatMap(deployUsers)

    def deployUsers(resolvedServer: ResolvedServer): Iterable[DeployUser]

  }


  sealed trait InfraStructureDeployable extends Deployable {
    def systemState: M[SystemState]
  }

  case class AppDeployable(domainName: DomainName, resolvedAppOpt: Option[ResolvedApp], versionBranch: VersionBranch) extends Deployable {

    def versionOpt: Option[Version] =
      versionBranch match {
        case VersionBranch.Empty =>
          None
        case VersionBranch.VersionBranchImpl(version, branch) =>
          Some(version)
      }

    def branchNameOpt: Option[BranchName] =
      versionBranch match {
        case VersionBranch.Empty =>
          None
        case VersionBranch.VersionBranchImpl(version, branch) =>
          branch
      }

    override def errorMessages: List[String] =
      resolvedAppOpt match {
        case None =>
          List(s"Application $domainName not found")
        case Some(resolvedApp) =>
          Nil
      }

    def resolvedApp = resolvedAppOpt.getOrError(z"cannot find app for ${domainName}")
    override def deployId: DeployId = DeployId(z"install-${resolvedApp.name}")
    override def deployUsers(resolvedRepo: ResolvedRepository): Iterable[DeployUser] =
      Iterable(RegularUser(resolvedApp.user))
    override def originalArg: String = z"${domainName}${versionBranch.asCommandLineArg}:install"
    override def localDeployArgs: Iterable[String] = domainName.value :: Nil
    override def systemState: M[SystemState] =
      ResolvedUser
        .live
        .flatMap(ru =>
          ApplicationInstallSync(ru.appsRootDirectory)
            .systemState(resolvedApp)
        )
  }

//  lazy val userDeployables = List(AuthorizeAutdKeys, ManagedKeys)
//  lazy val serverDeployables = List(PgbackrestClient, Caddy, Supervisor)
//  lazy val infraStructureDeployables = List(PgbackrestServer, RsnapshotServer, DnsDeployable, DatabaseDeployable)
//
//  lazy val allDeployables: Seq[Deployable] = userDeployables ++ serverDeployables ++ infraStructureDeployables

}


sealed trait Deployable {
  def errorMessages: List[String] = Nil
  def deployId: DeployId
  def deployUsers(resolvedRepo: ResolvedRepository): Iterable[DeployUser]
  def localDeployArgs: Iterable[String] = deployId.value :: Nil
  def systemState: M[SystemState]
  def originalArg: String
}
