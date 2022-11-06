package io.accur8.neodeploy


import a8.shared.{CascadingHocon, CompanionGen, ConfigMojo, ConfigMojoOps}
import a8.shared.FileSystem.{Directory, dir}
import model._
import a8.shared.SharedImports._
import a8.shared.json.ast.{JsDoc, JsObj, JsVal}
import io.accur8.neodeploy.Mxresolvedmodel.MxStoredSyncState
import io.accur8.neodeploy.Sync.SyncName


object resolvedmodel {

  case class ResolvedUser(
    descriptor: UserDescriptor,
    home: Directory,
    appsDirectory: Directory,
    server: ResolvedServer,
  ) {

    def personnel =
      descriptor
        .authorizedPersonnel
        .flatMap( personnelId =>
          server
            .repository
            .personnel(personnelId)
        )

    def authorizedKeys =
      descriptor
        .authorizedKeys
  }


  case class ResolvedServer(
    descriptor: ServerDescriptor,
    gitServerDirectory: GitServerDirectory,
    repository: ResolvedRepository,
  ) {

    def name = descriptor.name

    lazy val resolvedApps =
      gitServerDirectory
        .unresolvedDirectory
        .subdirs()
        .flatMap(loadResolvedAppFromDisk)

    def appsRootDirectory: AppsRootDirectory = descriptor.appInstallDirectory
    def supervisorDirectory: SupervisorDirectory = descriptor.supervisorDirectory
    def caddyDirectory: CaddyDirectory = descriptor.caddyDirectory

    def loadResolvedAppFromDisk(appConfigDir: Directory): Option[ResolvedApp] = {
      val appDescriptorFile = appConfigDir.file("application.json")
      appDescriptorFile
        .readAsStringOpt()
        .flatMap { appDescriptorJsonStr =>
          json.read[ApplicationDescriptor](appDescriptorJsonStr) match {
            case Left(e) =>
              logger.error(s"Failed to load application descriptor file: $appDescriptorFile -- ${e.prettyMessage}")
              None
            case Right(v) =>
              Some(ResolvedApp(v, this, appConfigDir))
          }
        }
    }
  }

  object ResolvedApp {

    def supervisorCommand(action: String, applicationName: ApplicationName): Command =
      Command(Seq(
        "supervisorctl",
        action,
        applicationName.value
      ))

  }

  case class ResolvedApp(
    application: ApplicationDescriptor,
    server: ResolvedServer,
    gitDirectory: Directory,
  ) {
  }


  object ResolvedRepository {

    def loadFromDisk(gitRootDirectory: GitRootDirectory): ResolvedRepository = {
      val cascadingHocon =
        CascadingHocon
          .loadConfigsInDirectory(gitRootDirectory.unresolvedDirectory.asNioPath, recurse = false)
          .resolve
      val configMojo =
        ConfigMojoOps.impl.ConfigMojoRoot(
          cascadingHocon.config.root(),
          cascadingHocon,
        )
      val repositoryDescriptor = configMojo.as[RepositoryDescriptor]
      ResolvedRepository(repositoryDescriptor, gitRootDirectory)
    }

  }

  case class ResolvedRepository(
    descriptor: RepositoryDescriptor,
    gitRootDirectory: GitRootDirectory,
  ) {

    def personnel(id: PersonnelId): Option[Personnel] = {
      val result =
        descriptor
          .personnel
          .find(_.id === id)
      if ( result.isEmpty ) {
        logger.warn(s"Personnel not found: $id")
      }
      result
    }

    lazy val servers =
      descriptor
        .servers
        .map { serverDescriptor =>
          ResolvedServer(
            serverDescriptor,
            GitServerDirectory(gitRootDirectory.unresolvedDirectory.subdir(serverDescriptor.name.value).asNioPath.toString),
            this,
          )
        }
  }


  object StoredSyncState extends MxStoredSyncState {
    def apply(appName: ApplicationName, applicationDescriptor: ApplicationDescriptor, states: Seq[(SyncName,Option[JsVal])]): StoredSyncState = {
      val statesJsoValues =
        states
          .flatMap {
            case (syncName, Some(state)) =>
              Some(syncName.value -> state)
            case _ =>
              None
          }
          .toMap

      new StoredSyncState(
        appName.value,
        applicationDescriptor.toJsDoc,
        JsObj(statesJsoValues).toJsDoc,
      )
    }

    def apply(userLogin: UserLogin, userDescriptor: UserDescriptor, states: Seq[(SyncName, Option[JsVal])]): StoredSyncState = {
      val statesJsoValues =
        states
          .flatMap {
            case (syncName, Some(state)) =>
              Some(syncName.value -> state)
            case _ =>
              None
          }
          .toMap

      new StoredSyncState(
        userLogin.value,
        userDescriptor.toJsDoc,
        JsObj(statesJsoValues).toJsDoc,
      )
    }

  }
  @CompanionGen
  case class StoredSyncState(
    name: String,
    descriptor: JsDoc,
    states: JsDoc,
  ) {
    def syncState(name: SyncName): Option[JsVal] =
      states.actualJsVal match {
        case jso: JsObj =>
          jso
            .values
            .get(name.value)
        case _ =>
          None
      }
  }

}
