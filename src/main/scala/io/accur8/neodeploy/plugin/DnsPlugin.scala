package io.accur8.neodeploy.plugin

import a8.shared.json.ast
import a8.shared.json.ast.JsNothing
import io.accur8.neodeploy.DomainNameSystem.{SyncRequest, defaultTtl}
import io.accur8.neodeploy.Sync.SyncName
import io.accur8.neodeploy.model.{DomainName, ManagedDomain}
import io.accur8.neodeploy.resolvedmodel.VirtualHost
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemStateModel.M
import io.accur8.neodeploy.{AmazonRoute53DnsApi, DomainNameSystem, Sync, UserPlugin, resolvedmodel}
import zio.{Task, ZIO}
import a8.shared.SharedImports._

object DnsPlugin extends RepositoryPlugins.RepositoryPlugin {

  override val name: Sync.SyncName = SyncName("dns")

  override def descriptorJson: ast.JsVal = JsNothing

  override def systemState(repo: resolvedmodel.ResolvedRepository): M[SystemState] =
    for {
      virtualHosts <- repo.virtualHosts
      ss <- run(repo, virtualHosts)
    } yield ss


  def run(repo: resolvedmodel.ResolvedRepository, virtualHosts: Vector[VirtualHost]): M[SystemState] = {

    val records =
      for {
        virtualHost <- virtualHosts
        domainName <- virtualHost.virtualNames
        managedDomain <- repo.findManagedDomain(domainName)
      } yield
        SystemState.DnsRecord(
          recordType = "CNAME",
          name = domainName,
          values = Vector(virtualHost.serverName.value),
          ttl = defaultTtl,
        )

    zsucceed(
      SystemState.Composite(
        "dns setup",
        records,
      )
    )

  }

}
