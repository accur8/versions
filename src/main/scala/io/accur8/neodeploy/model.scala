package io.accur8.neodeploy


import io.accur8.neodeploy.VFileSystem.{Directory, File, Symlink, dir}
import a8.shared.{CascadingHocon, CompanionGen, ConfigMojo, Exec, LongValue, StringValue, ZString}
import io.accur8.neodeploy.Mxmodel.*
import a8.shared.SharedImports.*
import a8.shared.ZString.ZStringer
import a8.shared.json.ast.{JsArr, JsDoc, JsNothing, JsObj, JsStr, JsVal, resolveAliases}
import a8.shared.json.{EnumCodecBuilder, JsonCodec, JsonTypedCodec, UnionCodecBuilder}
import a8.versions.RepositoryOps.RepoConfigPrefix
import sttp.model.Uri
import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedAuthorizedKey, ResolvedRepository, ResolvedUser}
import zio.process.CommandError
import zio.process.CommandError.NonZeroErrorCode
import zio.{Chunk, ExitCode, UIO, ZIO, ZLayer}

import scala.collection.Iterable
import PredefAssist.*
import io.accur8.neodeploy.model.DockerDescriptor.UninstallAction
import io.accur8.neodeploy.systemstate.SystemStateModel.M
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import a8.Scala3Hacks.*
import io.accur8.neodeploy.LocalDeploy.Config
import io.accur8.neodeploy.systemstate.SystemState.RunCommandState
import io.accur8.neodeploy.systemstate.{SystemState, SystemdLauncherMixin}
import systemstate.SystemStateModel.Command
import a8.shared.jdbcf.DatabaseConfig.Password
import a8.versions.model.BranchName
import org.rogach.scallop.{Scallop, ScallopOption}

object model extends LoggingF {

  object ListenPort extends LongValue.Companion[ListenPort]
  case class ListenPort(value: Long) extends LongValue

  object Version extends StringValue.Companion[Version]
  case class Version(value: String) extends StringValue

  object VersionBranch {

    given CanEqual[VersionBranch, VersionBranch] = CanEqual.derived

    case object Empty extends VersionBranch

    def fromAppArgs(versionArg: ScallopOption[String], branchArg: ScallopOption[String]): VersionBranch =
      versionArg.toOption match {
        case Some(v) =>
          VersionBranchImpl(
            Version(v),
            branchArg.toOption.map(BranchName(_))
          )
        case None =>
          Empty
      }

    case class VersionBranchImpl(version: Version, branch: Option[BranchName]) extends VersionBranch {
      override def asCommandLineArg: String =
        z":${version}${branch.map(b => z":${b}").getOrElse("")}"
    }
  }
  sealed trait VersionBranch {
    def asCommandLineArg: String = ""
  }

  object JavaVersion extends LongValue.Companion[JavaVersion]
  case class JavaVersion(value: Long) extends LongValue

  object ApplicationName extends StringValue.Companion[ApplicationName] {
    given CanEqual[ApplicationName, ApplicationName] = CanEqual.derived
  }
  case class ApplicationName(value: String) extends StringValue

  object DomainName extends StringValue.Companion[DomainName] {

    def fromZoneFile(str: String): DomainName =
      if ( str.endsWith(".")) {
        DomainName(str.substring(0, str.length-1))
      } else {
        DomainName(str)
      }

  }

  case class DomainName(value: String) extends StringValue {

    def isSubDomainOf(topLevelDomain: DomainName): Boolean =
      value.toLowerCase.endsWith(topLevelDomain.value.toLowerCase)

    def asDottedName = value + "."

    lazy val topLevelDomain =
      DomainName(
        value
          .splitList("\\.")
          .drop(1)
          .mkString(".")
      )

  }

  object Organization extends StringValue.Companion[Organization] {
    given CanEqual[Organization, Organization] = CanEqual.derived
  }
  case class Organization(value: String) extends StringValue {
    def asCoursierOrg = coursier.Organization(value)
  }

  object Artifact extends StringValue.Companion[Artifact] {
    given CanEqual[Artifact, Artifact] = CanEqual.derived
  }
  case class Artifact(value: String) extends StringValue {
    def asCoursierModuleName = coursier.ModuleName(value)
  }

  object DirectoryValue {
//    implicit def zstringer[A <: DirectoryValue]: ZStringer[A] =
//      new ZStringer[A] {
//        override def toZString(a: A): ZString =
//          a.asNioPath.toFile.getAbsolutePath
//      }
  }

  abstract class DirectoryValue extends StringValue {

    lazy val resolved: M[Directory] = {
      val dirValue = unresolved
      dirValue
        .exists
        .map {
          case true =>
            dirValue.makeDirectories
          case false =>
            zunit
        }
        .as(dirValue)
    }

    lazy val unresolved: Directory = dir(value)

    def symlink(name: String): Symlink =
      unresolved.symlink(name)

    def subdir(path: String): Directory =
      unresolved.subdir(path)

    def file(path: String): File =
      unresolved.file(path)

    def exists: M[Boolean] =
      unresolved
        .zdir
        .flatMap(_.exists)

  }

  object RSnapshotRootDirectory extends StringValue.Companion[RSnapshotRootDirectory]
  case class RSnapshotRootDirectory(value: String) extends DirectoryValue

  object RSnapshotConfigDirectory extends StringValue.Companion[RSnapshotConfigDirectory]
  case class RSnapshotConfigDirectory(value: String) extends DirectoryValue

  object SupervisorDirectory extends StringValue.Companion[SupervisorDirectory]
  case class SupervisorDirectory(value: String) extends DirectoryValue

  object CaddyDirectory extends StringValue.Companion[CaddyDirectory] {
    val layer = zl_succeed(CaddyDirectory("/etc/caddy"))
  }
  case class CaddyDirectory(value: String) extends DirectoryValue

  object LocalRootDirectory extends StringValue.Companion[LocalRootDirectory] {
    val default = LocalRootDirectory("/")
    val layer = ZLayer(effect)
    val effect =
      zservice[Config]
        .map(_.rootDirectory)
  }
  case class LocalRootDirectory(value: String) extends DirectoryValue

  object AppsRootDirectory extends StringValue.Companion[AppsRootDirectory]
  case class AppsRootDirectory(value: String) extends DirectoryValue

  object AppsInfo {
    lazy val layer: ZLayer[UserLogin & ResolvedRepository & Config, Throwable, AppsInfo] = ZLayer(effectM)
    lazy val effectM: ZIO[UserLogin & ResolvedRepository & Config, Throwable, AppsInfo] =
      for {
        ru <- ResolvedUser.live
      } yield {
        AppsInfo(
          appsRoot = ru.appsRootDirectory
        )
      }
  }
  case class AppsInfo(
    appsRoot: AppsRootDirectory,
  ) {
    lazy val installsDir: VFileSystem.Directory = appsRoot.subdir(".installs")
    lazy val persistenceDir: VFileSystem.Directory = appsRoot.subdir(".data")
    lazy val nixHashCacheDir: VFileSystem.Directory = installsDir.subdir(".nixhashcache")
  }

  object GitServerDirectory extends StringValue.Companion[GitServerDirectory]
  case class GitServerDirectory(value: String) extends DirectoryValue

  object GitRootDirectory extends StringValue.Companion[GitRootDirectory]
  case class GitRootDirectory(value: String) extends DirectoryValue

  sealed trait Launcher {

    def installService: SystemState

    def startService: SystemState =
      runCommandState("start")

    def stopService: SystemState =
      runCommandState("stop")

    def runCommandState(action: String): SystemState =
      serviceCommand(action)
        .map { cmd =>
          RunCommandState(
            installCommands = Vector(cmd)
          )
        }
        .getOrElse(SystemState.Empty)

    def serviceCommand(action: String): Option[Command]

  }

  object Launcher {

    case class SystemdLauncher(resolvedApp: ResolvedApp, systemdDescriptor: SystemdDescriptor) extends Launcher with SystemdLauncherMixin {
    }

    case class SupervisorLauncher(resolvedApp: ResolvedApp, supervisorDescriptor: SupervisorDescriptor) extends Launcher with SupervisorLauncherMixin {
    }

    case class DockerLauncher(resolvedApp: ResolvedApp, dockerDescriptor: DockerDescriptor) extends Launcher with DockerLauncherMixin {
    }

    def apply(resolvedApp: ResolvedApp): Launcher =
      resolvedApp.descriptor.launcher match {
        case sd: SystemdDescriptor =>
          SystemdLauncher(resolvedApp, sd)
        case sd: SupervisorDescriptor =>
          SupervisorLauncher(resolvedApp, sd)
        case dd: DockerDescriptor =>
          DockerLauncher(resolvedApp, dd)
      }

  }

  sealed trait Install {
    def command(applicationDescriptor: ApplicationDescriptor, appDirectory: Directory, appsRootDirectory: AppsRootDirectory): Command
    def description: String
  }
  object Install {

    given CanEqual[Install, Install] = CanEqual.derived

    implicit val jsonCodec: JsonTypedCodec[Install, JsObj] =
      UnionCodecBuilder[Install]
        .typeFieldName("kind")
        .defaultType[JavaApp]
        .addType[JavaApp]("javaapp")
        .addType[Manual]("manual")
        .addSingleton("docker", Docker)
        .build


    case object Docker extends Install {
      override def command(applicationDescriptor: ApplicationDescriptor, appDirectory: Directory, appsRootDirectory: AppsRootDirectory): Command =
        Command(Iterable.empty)
      override def description: String = "docker"
    }

    object JavaApp extends MxJavaApp
    @CompanionGen
    case class JavaApp(
      organization: Organization,
      artifact: Artifact,
      version: Version,
        /** branch is only for initial install / version resolution */
      defaultBranch: Option[BranchName] = None,
      webappExplode: Boolean = true,
      jvmArgs: Iterable[String] = None,
      appArgs: Iterable[String] = Iterable.empty,
      mainClass: String,
      javaVersion: JavaVersion = JavaVersion(11),
      repository: Option[RepoConfigPrefix] = None,
    ) extends Install {

      override def description: String = s"$organization:$artifact:$version"

      override def command(applicationDescriptor: ApplicationDescriptor, appDirectory: Directory, appsRootDirectory: AppsRootDirectory): Command = {
        val appsRoot = appsRootDirectory
        val appDir = appsRoot.subdir(applicationDescriptor.name.value)
        val bin = appDir.subdir("bin").file(applicationDescriptor.name.value)
        Command(
          bin.absPath,
        ).inDirectory(appDir)
      }

    }

    object Manual extends MxManual {
    }
    @CompanionGen
    case class Manual(
      description: String = "manual install",
      command: Command,
    ) extends Install {
      override def command(applicationDescriptor: ApplicationDescriptor, appDirectory: Directory, appsRootDirectory: AppsRootDirectory): Command =
        command
    }

  }

  object OnCalendarValue extends StringValue.Companion[OnCalendarValue] {
    val hourly = OnCalendarValue("hourly")
    val daily = OnCalendarValue("daily")
  }
  case class OnCalendarValue(value: String) extends StringValue

  object SupervisorDescriptor extends MxSupervisorDescriptor {
    val empty = SupervisorDescriptor()
  }
  @CompanionGen
  case class SupervisorDescriptor(
    autoStart: Option[Boolean] = None,
    autoRestart: Option[Boolean] = None,
    startRetries: Option[Int] = None,
    startSecs: Option[Int] = None,
  ) extends LauncherDescriptor

  object SystemdDescriptor extends MxSystemdDescriptor {
  }
  @CompanionGen
  case class SystemdDescriptor(
    unitName: Option[String] = None,
    environment: Vector[String] = Vector.empty,
    onCalendar: Option[OnCalendarValue] = None,
    persistent: Option[Boolean] = None,
    `type`: String = "simple",
    enableService: Boolean = true,
  ) extends LauncherDescriptor

  object DockerDescriptor extends MxDockerDescriptor {
    sealed trait UninstallAction extends enumeratum.EnumEntry
    object UninstallAction extends enumeratum.Enum[UninstallAction] {
      given CanEqual[UninstallAction, UninstallAction] = CanEqual.derived
      val values = findValues
//      case object RemoveAndInstallOnChange extends UninstallAction
      case object Remove extends UninstallAction
      case object Stop extends UninstallAction
      implicit val jsonCodec: JsonCodec[UninstallAction] = EnumCodecBuilder(this)
    }
  }
  @CompanionGen
  case class DockerDescriptor(
    name: String,
    args: Vector[String],
    uninstallAction: UninstallAction = UninstallAction.Stop,
  ) extends LauncherDescriptor

  object LauncherDescriptor {
    implicit val jsonCodec: JsonTypedCodec[LauncherDescriptor, JsObj] =
      UnionCodecBuilder[LauncherDescriptor]
        .typeFieldName("kind")
        .defaultType[SupervisorDescriptor]
        .addType[SystemdDescriptor]("systemd")
        .addType[SupervisorDescriptor]("supervisor")
        .addType[DockerDescriptor]("docker")
        .build
  }

  sealed trait LauncherDescriptor

  object ApplicationDescriptor extends MxApplicationDescriptor {
  }
  @CompanionGen
  case class ApplicationDescriptor(
    name: ApplicationName,
    install: Install,
    caddyConfig: Option[String] = None,
    listenPort: Option[ListenPort] = None,
    stopServerCommand: Option[Command] = None,
    startServerCommand: Option[Command] = None,
    domainName: Option[DomainName] = None,
    domainNames: Vector[DomainName] = Vector.empty,
    setup: ApplicationSetupDescriptor = ApplicationSetupDescriptor.empty,
//    restartOnCalendar: Option[OnCalendarValue] = None,
//    startOnCalendar: Option[OnCalendarValue] = None,
    launcher: LauncherDescriptor = SupervisorDescriptor.empty,
  ) {
    def resolvedDomainNames: Vector[DomainName] = domainNames ++ domainName
  }

  object ApplicationSetupDescriptor extends MxApplicationSetupDescriptor {
    val empty = ApplicationSetupDescriptor()
  }
  @CompanionGen
  case class ApplicationSetupDescriptor(
    database: Option[DatabaseSetupDescriptor] = None,
    qubes: Option[DomainName] = None,
  )


  object UserPassword extends MxUserPassword
  @CompanionGen
  case class UserPassword(user: UserLogin, rawPassword: String) {
    def password = Password(rawPassword)
  }

  object Passwords extends MxPasswords
  @CompanionGen
  case class Passwords(userPasswords: Vector[UserPassword]) {
    lazy val userPasswordsMap = userPasswords.map(up => up.user -> up.rawPassword).toMap

    def apply(user: UserLogin): Password =
      Password(userPasswordsMap(user))

    def apply(user: DatabaseUserDescriptor): Password =
      Password(userPasswordsMap(user.name))

    def credentials(user: UserLogin): UserPassword =
      UserPassword(user, userPasswordsMap(user))
  }

  object DatabaseSetupDescriptor extends MxDatabaseSetupDescriptor {
  }
  @CompanionGen
  case class DatabaseSetupDescriptor(
    databaseServer: DomainName,
    databaseName: DatabaseName,
    owner: UserLogin,
    extraUsers: Iterable[DatabaseUserDescriptor] = Iterable.empty,
//    zooFiles: Iterable[ZooFile] = Iterable.empty,
  ) {
    def allUsers: Iterable[UserLogin] = (extraUsers.map(_.name) ++ Some(owner))
  }

  object DatabaseUserDescriptor extends MxDatabaseUserDescriptor {
  }
  @CompanionGen
  case class DatabaseUserDescriptor(
    name: UserLogin,
    roles: Iterable[DatabaseUserRole],
  )

  object UserLogin extends StringValue.Companion[UserLogin] {
    given CanEqual[UserLogin, UserLogin] = CanEqual.derived
    val root = UserLogin("root")
    def thisUser(): UserLogin =
      UserLogin(System.getProperty("user.name"))
  }
  case class UserLogin(value: String) extends StringValue

  object ZooFile extends MxZooFile {
  }
  @CompanionGen
  case class ZooFile(
    filename: String,
    organization: Organization,
    artifact: Artifact,
    zooVersion: Option[String] = None,
  )

  object DatabaseName extends StringValue.Companion[DatabaseName]
  case class DatabaseName(value: String) extends StringValue

  object DatabaseUserRole extends StringValue.Companion[DatabaseUserRole]
  case class DatabaseUserRole(value: String) extends StringValue

  object UserDescriptor extends MxUserDescriptor
  @CompanionGen
  case class UserDescriptor(
    login: UserLogin,
    aliases: Vector[QualifiedUserName] = Vector.empty,
    authorizedKeys: Vector[QualifiedUserName] = Vector.empty,
    a8VersionsExec: Option[String] = None,
    home: Option[Directory] = None,
    manageSshKeys: Boolean = true,
    appInstallDirectory: Option[AppsRootDirectory] = None,
    plugins: JsDoc = JsDoc.empty,
  ) {
    def resolvedAppsRootDirectory = appInstallDirectory.getOrElse(AppsRootDirectory(resolvedHome.subdir("apps").path))
    def resolvedHome = home.getOrElse(dir(z"/home/${login}"))
  }


  object ServerName extends StringValue.Companion[ServerName] {
    given CanEqual[ServerName, ServerName] = CanEqual.derived
    def thisServer(): ServerName =
      ServerName(
        Exec("hostname")
          .execCaptureOutput()
          .stdout
          .splitList("\\.")
          .head
    )
  }
  case class ServerName(value: String) extends StringValue

  object RSnapshotClientDescriptor extends MxRSnapshotClientDescriptor
  @CompanionGen
  case class RSnapshotClientDescriptor(
    name: String,
    directories: Vector[String],
    runAt: OnCalendarValue,
    hourly: Boolean = false,
    includeExcludeLines: Iterable[String] = Iterable.empty,
  ) {
  }

  object RSnapshotServerDescriptor extends MxRSnapshotServerDescriptor
  @CompanionGen
  case class RSnapshotServerDescriptor(
    name: String,
    snapshotRootDir: RSnapshotRootDirectory,
    configDir: RSnapshotConfigDirectory,
    logDir: String = "/var/log",
    runDir: String = "/var/run",
  )

  object PgbackrestClientDescriptor extends MxPgbackrestClientDescriptor
  @CompanionGen
  case class PgbackrestClientDescriptor(
    name: String,
    pgdata: String,
    stanzaNameOverride: Option[String] = None,
    onCalendar: Option[OnCalendarValue] = None,
    configFile: Option[String] = None,
  ) {
  }

  object PgbackrestServerDescriptor extends MxPgbackrestServerDescriptor
  @CompanionGen
  case class PgbackrestServerDescriptor(
    name: String,
    configHeader: String,
    configFile: Option[String] = None,
  )


  object ServerDescriptor extends MxServerDescriptor
  @CompanionGen
  case class ServerDescriptor(
    name: ServerName,
    aliases: Iterable[ServerName] = Iterable.empty,
//    supervisorDirectory: SupervisorDirectory,
//    caddyDirectory: CaddyDirectory,
    publicDomainName: Option[DomainName] = None,
    vpnDomainName: DomainName,
    reverseProxyName: Option[DomainName] = None,
    users: Vector[UserDescriptor],
    a8VersionsExec: Option[String] = None,
    supervisorctlExec: Option[String] = None,
  ) {
    lazy val names = Option(name) ++ aliases
  }

  object AuthorizedKey extends StringValue.Companion[AuthorizedKey]
  case class AuthorizedKey(value: String) extends StringValue

  object PublicKey extends StringValue.Companion[PublicKey]
  case class PublicKey(value: String) extends StringValue {
    def asAuthorizedKey = AuthorizedKey(value)
  }

  object AwsSecretKey extends StringValue.Companion[AwsSecretKey]
  case class AwsSecretKey(value: String) extends StringValue

  object AwsAccessKey extends StringValue.Companion[AwsAccessKey]
  case class AwsAccessKey(value: String) extends StringValue

  object AwsCredentials extends MxAwsCredentials
  @CompanionGen
  case class AwsCredentials(
    awsSecretKey: AwsSecretKey,
    awsAccessKey: AwsAccessKey,
  ) {

    def asAmazonSdkCredentials =
      AwsBasicCredentials.create(awsAccessKey.value, awsSecretKey.value)

    def asAmazonSdkCredentialsProvider =
      StaticCredentialsProvider.create(asAmazonSdkCredentials)

  }

  object ManagedDomain extends MxManagedDomain
  @CompanionGen
  case class ManagedDomain(topLevelDomains: Vector[DomainName], awsCredentials: AwsCredentials)

  object RepositoryDescriptor extends MxRepositoryDescriptor
  @CompanionGen
  case class RepositoryDescriptor(
    publicKeys: Iterable[Personnel] = Iterable.empty,
    servers: Vector[ServerDescriptor],
    healthchecksApiToken: HealthchecksDotIo.ApiAuthToken,
    managedDomains: Vector[ManagedDomain] = Vector.empty,
    plugins: JsDoc = JsDoc.empty,
  ) {
    def serversAndUsers =
      servers
        .flatMap(server => server.users.map(server -> _))
  }

  object QualifiedUserName extends StringValue.Companion[QualifiedUserName]
  case class QualifiedUserName(value: String) extends StringValue

  object Personnel extends MxPersonnel
  @CompanionGen
  case class Personnel(
    id: QualifiedUserName,
    description: String,
    authorizedKeysUrl: Option[String] = None,
    authorizedKeys: Iterable[AuthorizedKey] = None,
    members: Iterable[QualifiedUserName] = Iterable.empty,
  ) {
    lazy val resolvedAuthorizedKeys =
      authorizedKeys
        .map(ak => ResolvedAuthorizedKey(id.value, ak))
  }

}
