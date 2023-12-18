package io.accur8.neodeploy

import a8.shared.SharedImports.*
import a8.shared.app.BootstrappedIOApp.BootstrapEnv
import a8.shared.app.{BootstrapConfig, BootstrappedIOApp}
import a8.shared.{CompanionGen, FileSystem, ZFileSystem}
import io.accur8.neodeploy.LocalDeploy.Config
import io.accur8.neodeploy.model.*
import io.accur8.neodeploy.resolvedmodel.{ResolvedRepository, ResolvedServer, ResolvedUser}
import io.accur8.neodeploy.systemstate.SystemStateModel
import io.accur8.neodeploy.systemstate.SystemStateModel.*
import zio.{ZIO, ZLayer}

import java.net.InetAddress

object LocalDeploySubCommand {

}

case class LocalDeploySubCommand(
                                  deployArgs: ResolvedDeployables,
                                  dryRun: Boolean,
) {

  def runM: M[Unit] = {
    val effect =
      for {
        config <- zservice[Config]
        user <- zservice[ResolvedUser]
        _ <- LocalDeploy(user, deployArgs, config, dryRun).run
      } yield ()

    effect
      .provideSome[SystemStateModel.Environ](
        Layers.resolvedUserL,
        Layers.resolvedServerL,
      )

  }

}
