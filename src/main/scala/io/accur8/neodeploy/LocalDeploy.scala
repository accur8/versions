package io.accur8.neodeploy

import a8.shared.SharedImports.*
import a8.shared.ZFileSystem.Directory
import a8.common.logging.{Logging, LoggingF}
import a8.shared.json.ast
import a8.shared.json.ast.{JsDoc, JsObj, JsVal}
import a8.shared.{CompanionGen, FileSystem, StringValue}
import io.accur8.neodeploy.DeployUser.RegularUser
import io.accur8.neodeploy.LocalDeploy.Config
import io.accur8.neodeploy.MxLocalDeploy.MxConfig
import io.accur8.neodeploy.model.*
import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedServer, ResolvedUser}
import io.accur8.neodeploy.systemstate.SystemStateModel.*
import zio.{Task, UIO, ZIO}

object LocalDeploy {

  object Config extends MxConfig {
    def default() =
      Config(
        GitRootDirectory(FileSystem.userHome.subdir("server-app-configs").asNioPath.toAbsolutePath.toString),
        ServerName.thisServer(),
        userLogin = UserLogin.thisUser(),
      )
  }

  @CompanionGen
  case class Config(
    gitRootDirectory: GitRootDirectory,
    serverName: ServerName,
    userLogin: UserLogin = UserLogin.thisUser(),
  )

}

case class LocalDeploy(resolvedUser: ResolvedUser, deployArgs: ResolvedDeployArgs, config: Config) extends LoggingF {

  lazy val resolvedServer = resolvedUser.server

  lazy val stateDirectory: Directory =
    resolvedUser
      .home
      .subdir(".neodeploy-state")

  lazy val healthchecksDotIo = HealthchecksDotIo(resolvedServer.repository.descriptor.healthchecksApiToken)

  def appSyncRun: ApplyState[Unit] =
    loggerF.debug("appSyncRun") *>
      SyncContainer(stateDirectory, RegularUser(resolvedUser), deployArgs.args.toVector)
        .run

  def run: ApplyState[Unit] =
    for {
      _ <- loggerF.info(z"running for ${resolvedUser.qualifiedUserName}")
      _ <- loggerF.debug(z"resolved user ${resolvedUser.qualifiedUserName} -- ${resolvedUser.descriptor.prettyJson.indent("    ")}")
      _ <- loggerF.debug(z"resolved user plugins ${resolvedUser.qualifiedUserName} -- ${resolvedUser.plugins.descriptorJson.prettyJson.indent("    ")}")
      _ <- appSyncRun
    } yield ()

}