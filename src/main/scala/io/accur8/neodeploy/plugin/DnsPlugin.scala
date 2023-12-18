package io.accur8.neodeploy.plugin

import a8.shared.json.ast
import a8.shared.json.ast.JsNothing
import io.accur8.neodeploy.DomainNameSystem.{SyncRequest, defaultTtl}
import io.accur8.neodeploy.model.{DomainName, ManagedDomain}
import io.accur8.neodeploy.resolvedmodel.{ResolvedRepository, VirtualHost}
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemStateModel.M
import io.accur8.neodeploy.{AmazonRoute53DnsApi, DomainNameSystem, UserPlugin, resolvedmodel}
import zio.{Task, ZIO}
import a8.shared.SharedImports.*
import a8.common.logging.LoggingF

object DnsPlugin extends LoggingF {

  def systemState: M[SystemState] = {
    zservice[ResolvedRepository]
      .flatMap { repo =>
        val virtualHosts = repo.virtualHosts
        for {
          ss <- run(repo, virtualHosts)
        } yield ss
      }
  }

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

    val dupes =
      (records)
        .groupBy(_.name)
        .filter(_._2.size > 1)
        .map(_._1)

    val logDupesEffect =
        if (dupes.isEmpty)
          zunit
        else {
          loggerF.warn(s"found duplicate dns records system will vacillate between these dns record(s) ${dupes}")
        }

    logDupesEffect
      .as(
        SystemState.Composite(
          "dns setup",
          records,
        )
      )

  }

}
