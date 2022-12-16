package io.accur8.neodeploy.systemstate


import a8.shared.{CompanionGen, ZFileSystem}
import a8.shared.json.ast.JsObj
import a8.shared.json.{JsonCodec, JsonTypedCodec, UnionCodecBuilder, ast}
import io.accur8.neodeploy.{HealthchecksDotIo, LazyJsonCodec}
import io.accur8.neodeploy.model.Install.JavaApp
import io.accur8.neodeploy.model.{ApplicationDescriptor, DockerDescriptor, UserLogin}
import io.accur8.neodeploy.systemstate.MxSystemState._
import io.accur8.neodeploy.systemstate.SystemStateModel._
import zio.Chunk
import a8.shared.SharedImports._

object SystemState {

  object TextFile extends MxTextFile
  @CompanionGen
  case class TextFile(
    filename: String,
    contents: String,
    perms: UnixPerms = UnixPerms.empty,
  ) extends NoSubStates with TextFileContentsMixin {
    override def prefix: String = ""
  }

  object SecretsTextFile extends MxSecretsTextFile {
  }
  @CompanionGen
  case class SecretsTextFile(
    filename: String,
    secretContents: SecretContent,
    perms: UnixPerms = UnixPerms.empty,
  ) extends NoSubStates with TextFileContentsMixin {
    override def contents: String = secretContents.value
    override def prefix: String = "secret "
  }

  object JavaAppInstall extends MxJavaAppInstall
  @CompanionGen
  case class JavaAppInstall(
    appInstallDir: String,
    fromRepo: JavaApp,
    descriptor: ApplicationDescriptor,
    gitAppDirectory: String,
  ) extends NoSubStates with JavaAppInstallMixin

  object Directory extends MxDirectory
  @CompanionGen
  case class Directory(
    path: String,
    perms: UnixPerms = UnixPerms.empty,
  ) extends NoSubStates with DirectoryMixin

  case object Empty extends NoSubStates {
    override def stateKey = None
    override def dryRunInstall: Vector[String] = Vector.empty
    override def isActionNeeded = zsucceed(false)
    override def runApplyNewState = zunit
    override def runUninstallObsolete = zunit
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
        .addType[Composite]("composite")
        .addType[Directory]("directory")
        .addType[DockerState]("docker")
        .addType[HealthCheck]("healthcheck")
        .addType[JavaAppInstall]("javaappinstall")
        .addType[RunCommandState]("runcommand")
        .addType[SecretsTextFile]("secretstextfile")
        .addType[TextFile]("textfile")
        .addType[TriggeredState]("triggeredstate")
        .build
    )

}

sealed trait SystemState extends SystemStateMixin {
}


