package io.accur8.neodeploy.plugin

import a8.shared.SharedImports._
import a8.shared.ZFileSystem
import a8.shared.json.ast.{JsNothing, JsVal}
import io.accur8.neodeploy.Sync.SyncName
import io.accur8.neodeploy.model.{DomainName, ListenPort}
import io.accur8.neodeploy.resolvedmodel.ResolvedUser
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemStateModel.{M, UnixPerms}
import io.accur8.neodeploy.{Overrides, UserPlugin}

case object CaddyServerPlugin extends UserPlugin {

  def configSnippet(listenPort: ListenPort, domains: Iterable[DomainName]): String =
    z"""
${domains.map(_.value).mkString(", ")} {
  encode gzip
  reverse_proxy localhost:${listenPort}
}
      """.trim

  val name = SyncName("caddyServer")

  override def descriptorJson: JsVal = JsNothing

  override def systemState(input: ResolvedUser): M[SystemState] =
    zsucceed {
      val file = ZFileSystem.file("/ec/caddy/CaddyFile")
      val apps =
        input
          .server
          .resolvedUsers
          .flatMap(_.resolvedApps)
      val fileContents =
        apps
          .flatMap { app =>
            (app.descriptor.caddyConfig, app.descriptor.listenPort) match {
              case (Some(cc), _) =>
                cc.some
              case (_, Some(listenPort)) =>
                app
                  .descriptor
                  .resolvedDomainNames
                  .toNonEmpty
                  .map { domains =>
                    configSnippet(listenPort, domains).some
                  }
              case _ =>
                None
            }
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
            .asSystemStateCommand
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
