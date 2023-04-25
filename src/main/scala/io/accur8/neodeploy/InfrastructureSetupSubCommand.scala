package io.accur8.neodeploy


import io.accur8.neodeploy.model.{DomainName, ManagedDomain}
import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedRepository, ResolvedUser, VirtualHost}
import io.accur8.neodeploy.systemstate.SystemStateModel.{Environ, M, PreviousState}
import a8.shared.SharedImports._
import a8.shared.StringValue
import a8.shared.ZFileSystem.File
import a8.shared.app.BootstrappedIOApp.BootstrapEnv
import a8.shared.app.{BootstrappedIOApp, LoggingF}
import io.accur8.neodeploy.DomainNameSystem.{Record, SyncRequest, defaultTtl}
import io.accur8.neodeploy.InfrastructureSetupSubCommand.Name
import io.accur8.neodeploy.PushRemoteSyncSubCommand.Filter
import zio.{Task, ZIO}

object InfrastructureSetupSubCommand {
  object Name extends StringValue.Companion[Name]
  case class Name(value: String) extends StringValue
}

case class InfrastructureSetupSubCommand(resolvedRepository: ResolvedRepository) extends LoggingF {

  val syncContainerPrefix = SyncContainer.Prefix("repo")
  val stateDirectory = resolvedRepository.gitRootDirectory.subdir(".state").subdir("infrastructure")

  def run: Task[Unit] =
    Layers.provide(runM)

  def runM: M[Unit] =
    for {
      _ <- loggerF.debug(z"resolved repo plugins -- ${resolvedRepository.repositoryPlugins.descriptorJson.prettyJson.indent("    ")}")
      result <- repoSyncRun
    } yield ()

  def repoSyncRun: ZIO[Environ, Throwable, Unit] =
    SyncContainer
      .loadState(stateDirectory, syncContainerPrefix)
      .flatMap { previousStates =>
        SyncImpl(previousStates)
          .run
      }

  case class SyncImpl(previousStates: Vector[PreviousState]) extends SyncContainer[ResolvedRepository, Name](syncContainerPrefix, stateDirectory) {

    override def name(resolved: ResolvedRepository): Name = Name("repository")

    override def nameFromStr(nameStr: String): Name = Name(nameStr)

    override val newResolveds = Vector(resolvedRepository)

    override val staticSyncs: Seq[Sync[ResolvedRepository]] = Seq.empty

    override def filter(pair: NamePair): Boolean = true

    override def resolvedSyncs(resolved: ResolvedRepository): Seq[Sync[ResolvedRepository]] =
      resolved.repositoryPlugins.pluginInstances

    override def runSystemStateServicesCommit: M[Unit] =
      zservice[DnsService]
        .flatMap(_.commit) // hacky hack to commit the dns changes

  }

}
