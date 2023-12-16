package io.accur8.neodeploy

import a8.shared.SharedImports.*
import VFileSystem._
import a8.shared.ZString
import a8.common.logging.LoggingF
import io.accur8.neodeploy.model.CaddyDirectory
import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedServer}
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemStateModel.*
import zio.{Task, ZIO}

object CaddySync {

  val managedSubDirName = "managed"

  def configFile(caddyDir: CaddyDirectory, applicationDescriptor: model.ApplicationDescriptor): File =
    caddyDir
      .subdir(managedSubDirName)
      .file(z"${applicationDescriptor.name}.caddy")

  def caddyConfigContents(caddyDir: CaddyDirectory, applicationDescriptor: model.ApplicationDescriptor): Option[SystemState.TextFile] = {
    import applicationDescriptor._
    def result0 =
      for {
        listenPort <- applicationDescriptor.listenPort.toSeq
        _ <- applicationDescriptor.resolvedDomainNames.nonEmpty.toOption(())
      } yield
        z"""
${applicationDescriptor.resolvedDomainNames.map(_.value).mkString(", ")} {
  encode gzip
  reverse_proxy localhost:${listenPort}
}
""".trim

    applicationDescriptor
      .caddyConfig
      .orElse(
        result0.mkString("\n\n").some
      ).map(SystemState.TextFile(configFile(caddyDir, applicationDescriptor), _))

  }

  def mainConfig(caddyDirectory: CaddyDirectory): SystemState.TextFile =
    SystemState.TextFile(
      caddyDirectory.file("Caddyfile"),
      z"""
import ${caddyDirectory.value}/${managedSubDirName}/*
import ${caddyDirectory.value}/custom/*
      """.trim
    )

  def systemState(server: ResolvedServer): M[SystemState] =
    zservice[CaddyDirectory].map { caddyDir =>

      val rawFiles =
        server
          .applications
          .flatMap(ra => caddyConfigContents(caddyDir, ra.descriptor))

      val reloadCaddyCommand =
        Overrides.sudoSystemCtlCommand
          .appendArgs("reload", "caddy")

      val filesState =
        SystemState.Composite(
          z"caddy files",
          rawFiles ++ Vector(mainConfig(caddyDir)),
        )

      SystemState.Composite(
        z"caddy setup",
        Vector(SystemState.TriggeredState(
          triggerState = filesState,
          postTriggerState =
            SystemState.RunCommandState(
              stateKey = StateKey("caddy", "caddy").some,
              installCommands = Vector(reloadCaddyCommand),
              uninstallCommands = Vector(reloadCaddyCommand),
            )
        ))
      )

    }


}

