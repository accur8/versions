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
    def result0: Option[String] =
      for {
        listenPort <- applicationDescriptor.listenPort
        domains <- applicationDescriptor.resolvedDomainNames.toNonEmpty
      } yield
        z"""
${domains.map(_.value).mkString(", ")} {
  encode gzip
  reverse_proxy localhost:${listenPort}
}
""".trim

    applicationDescriptor
      .caddyConfig
      .orElse(
        result0
      ).map(SystemState.TextFile(configFile(caddyDir, applicationDescriptor), _))

  }

  def mainConfig(caddyDirectory: CaddyDirectory): SystemState.TextFile =
    SystemState.TextFile(
      caddyDirectory.file("Caddyfile"),
      z"""
import ${caddyDirectory.value}/${managedSubDirName}/*.caddy
import ${caddyDirectory.value}/custom/*.caddy
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

      val createCustomDir =
        SystemState.TextFile(
          caddyDir.subdir("custom").file(".placeholder"),
          "a place holder file to get the custom directory created",
        )

      SystemState.Composite(
        z"caddy setup",
        Vector(
          SystemState.TriggeredState(
            triggerState = filesState,
            postTriggerState =
              SystemState.RunCommandState(
                stateKey = StateKey("caddy", "caddy").some,
                installCommands = Vector(reloadCaddyCommand),
                uninstallCommands = Vector(reloadCaddyCommand),
              )
          ),
          createCustomDir,
        )
      )

    }


}

