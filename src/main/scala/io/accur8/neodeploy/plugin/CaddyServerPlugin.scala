package io.accur8.neodeploy.plugin

import a8.shared.SharedImports.*
import a8.shared.json.ast.{JsNothing, JsVal}
import io.accur8.neodeploy.model.{DomainName, ListenPort}
import io.accur8.neodeploy.resolvedmodel.{ResolvedServer, ResolvedUser}
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemStateModel.{M, UnixPerms}
import io.accur8.neodeploy.{Overrides, UserPlugin}
import io.accur8.neodeploy.VFileSystem

case object CaddyServerPlugin {

  def configSnippet(listenPort: ListenPort, domains: Iterable[DomainName]): String =
    z"""
${domains.map(_.value).mkString(", ")} {
  encode gzip
  reverse_proxy localhost:${listenPort}
}
      """.trim

  def systemState(resolvedServer: ResolvedServer): M[SystemState] =
    zsucceed {
      val file = VFileSystem.file("/etc/caddy/Caddyfile")
      val apps =
        resolvedServer
          .resolvedUsers
          .flatMap(_.resolvedApps)
      println(s"${apps.map(_.name).mkString(" ")}")
      val fileContents: String =
        apps
          .flatMap { app =>
            val caddyAppConfig: Option[String] =
            (app.descriptor.caddyConfig, app.descriptor.listenPort) match {
              case (Some(cc), _) =>
                cc.some
              case (_, Some(listenPort)) =>
                app
                  .descriptor
                  .resolvedDomainNames
                  .toNonEmpty
                  .map { domains =>
                    configSnippet(listenPort, domains)
                  }
              case _ =>
                None
            }
            caddyAppConfig
          }
          .mkString("\n\n")

      val state =
        SystemState.TextFile(
          file,
          fileContents,
          UnixPerms.empty,
        )

      val reloadCaddy =
        Vector(
          Overrides.systemCtlCommand
            .appendArgs("reload", "caddy")
        )

      val runReloadCaddyCommand =
        SystemState.RunCommandState(
          installCommands = reloadCaddy,
          uninstallCommands = reloadCaddy,
        )

      SystemState.TriggeredState(
        triggerState = state,
        postTriggerState = runReloadCaddyCommand,
      )
    }

}
