package io.accur8.neodeploy.systemstate


import a8.shared.CompanionGen
import a8.shared.json.ast.JsObj
import a8.shared.json.{JsonCodec, JsonTypedCodec, UnionCodecBuilder, ast}
import io.accur8.neodeploy.{DatabaseSetupMixin, HealthchecksDotIo, LazyJsonCodec, VFileSystem}
import io.accur8.neodeploy.model.Install.JavaApp
import io.accur8.neodeploy.model.{ApplicationDescriptor, DatabaseName, DatabaseUserDescriptor, DockerDescriptor, DomainName, Passwords, UserLogin}
import io.accur8.neodeploy.systemstate.MxSystemState.*
import io.accur8.neodeploy.systemstate.SystemStateModel.*
import zio.Chunk
import a8.shared.SharedImports.*
import a8.shared.jdbcf.DatabaseConfig.Password
import io.accur8.neodeploy.VFileSystem.PathName

object SystemState {

  given CanEqual[SystemState, SystemState] = CanEqual.derived

  def statesByKey(state: SystemState): Map[StateKey,SystemState] = {
    val thisEntry =
      state
        .stateKey
        .map(_ -> state)
    val substateEntries =
      state match {
        case hss: HasSubStates =>
          hss
            .subStates
            .flatMap(statesByKey)
        case _ =>
          Map.empty
      }
    (substateEntries ++ thisEntry)
      .toMap
  }

  object Symlink extends MxSymlink
  @CompanionGen
  case class Symlink(
    target: String,
    link: VFileSystem.Symlink,
    perms: UnixPerms = UnixPerms.empty,
  ) extends NoSubStates with SymlinkMixin {
    def targetPath = PathName(target)
  }

  object TextFile extends MxTextFile
  @CompanionGen
  case class TextFile(
    file: VFileSystem.File,
    contents: String,
    perms: UnixPerms = UnixPerms.empty,
  ) extends NoSubStates with TextFileContentsMixin {
    override def prefix: String = ""
  }

  object SecretsTextFile extends MxSecretsTextFile {
  }
  @CompanionGen
  case class SecretsTextFile(
    file: VFileSystem.File,
    secretContents: SecretContent,
    perms: UnixPerms = UnixPerms.empty,
  ) extends NoSubStates with TextFileContentsMixin {
    override def contents: String = secretContents.value
    override def prefix: String = "secret "
  }

  object DatabaseSetup extends MxDatabaseSetup {
  }
  @CompanionGen
  case class DatabaseSetup(
    databaseServer: DomainName,
    databaseName: DatabaseName,
    owner: UserLogin,
    passwords: Passwords,
    extraUsers: Iterable[DatabaseUserDescriptor] = Iterable.empty,
  ) extends NoSubStates with DatabaseSetupMixin {
  }

  object JavaAppInstall extends MxJavaAppInstall
  @CompanionGen
  case class JavaAppInstall(
    canonicalAppDir: VFileSystem.Symlink,
    fromRepo: JavaApp,
    descriptor: ApplicationDescriptor,
    gitAppDirectory: VFileSystem.Directory,
    startService: SystemState,
    stopService: SystemState,
    // a temp hack so JavaAppInstall's are never the same so they are always deployed
//    neverTheSame: Long = System.currentTimeMillis(),
  ) extends NoSubStates with JavaAppInstallMixin {
  }

  object Directory extends MxDirectory
  @CompanionGen
  case class Directory(
    path: VFileSystem.Directory,
    perms: UnixPerms = UnixPerms.empty,
  ) extends NoSubStates with DirectoryMixin

  case object Empty extends NoSubStates {
    override def stateKey = None
    override def dryRunInstall: Vector[String] = Vector.empty
    override def isActionNeeded = zsucceed(false)
    override def runApplyNewState = zunit
    override def runUninstallObsolete(interpreter: Interpreter) = zunit
  }

  object Composite extends MxComposite {

    implicit lazy val jsonCodec: JsonTypedCodec[Composite, JsObj] =
      LazyJsonCodec(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.description)
          .addField(_.states)
          .build
      )

  }
  @CompanionGen(jsonCodec = false)
  case class Composite(
    description: String,
    states: Vector[SystemState],
  ) extends HasSubStates with CompositeMixin {
    override def subStates: Vector[SystemState] = states
  }

  object DnsRecord extends MxDnsRecord {
//    given CanEqual[DnsRecord, DnsRecord] = CanEqual.derived
  }
  @CompanionGen
  case class DnsRecord(
    name: DomainName,
    recordType: String,
    values: Vector[String],
    ttl: Long,
  ) extends NoSubStates with DnsRecordMixin

  object HealthCheck extends MxHealthCheck
  @CompanionGen
  case class HealthCheck(
    data: HealthchecksDotIo.CheckUpsertRequest,
  ) extends NoSubStates with HealthCheckMixin

  sealed trait NoSubStates extends SystemState {
  }

  sealed trait HasSubStates extends SystemState {
    def subStates: Vector[SystemState]
  }

  object RunCommandState extends MxRunCommandState
  @CompanionGen
  case class RunCommandState(
    override val stateKey: Option[StateKey] = None,
    installCommands: Vector[Command] = Vector.empty,
    uninstallCommands: Vector[Command] = Vector.empty,
  ) extends NoSubStates with RunCommandStateMixin


  object DockerState extends MxDockerState
  @CompanionGen
  case class DockerState(
    descriptor: DockerDescriptor,
  ) extends NoSubStates with DockerStateMixin


  object TriggeredState extends MxTriggeredState
  /**
   * if any changes are needed in triggerState then preTriggeredState will get applied
   * then triggerState then postTriggerState
   * for example for systemd the triggerState are all the systemd unit files
   * and the postTriggerState is the systemd daemon reload and enable commands
   */
  @CompanionGen
  case class TriggeredState(
    preTriggerState: SystemState = SystemState.Empty,
    postTriggerState: SystemState = SystemState.Empty,
    triggerState: SystemState = SystemState.Empty,
  ) extends SystemState with TriggeredStateMixin


  implicit lazy val jsonCodec: JsonTypedCodec[SystemState, ast.JsObj] =
    LazyJsonCodec(
      UnionCodecBuilder[SystemState]
        .typeFieldName("kind")
        .addSingleton("empty", Empty)
        .addType[Symlink]("symlink")
        .addType[Composite]("composite")
        .addType[Directory]("directory")
        .addType[DockerState]("docker")
        .addType[HealthCheck]("healthcheck")
        .addType[JavaAppInstall]("javaappinstall")
        .addType[RunCommandState]("runcommand")
        .addType[SecretsTextFile]("secretstextfile")
        .addType[TextFile]("textfile")
        .addType[TriggeredState]("triggeredstate")
        .addType[DnsRecord]("dnsrecord")
        .addType[DatabaseSetup]("database")
        .build
    )

}

sealed trait SystemState extends SystemStateMixin {
}


