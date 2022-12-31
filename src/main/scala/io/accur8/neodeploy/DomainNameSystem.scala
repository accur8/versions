package io.accur8.neodeploy

import a8.shared.CompanionGen
import io.accur8.neodeploy.model.DomainName
import org.typelevel.ci.CIString
import software.amazon.awssdk.services.route53.model.RRType
import zio.{Task, ZIO}
import a8.shared.SharedImports._
import io.accur8.neodeploy.systemstate.SystemState.DnsRecord

object DomainNameSystem {

  lazy val defaultTtl = 300L // 300 seconds

  type T[A] = Task[A]

  type Record = DnsRecord
  val Record = DnsRecord

  case class QueryResults(
    domainName: DomainName,
    currentState: Iterable[Record],
  )

  case class SyncRequest(
    domainName: DomainName,
    newState: Iterable[Record],
    previousState: Iterable[Record],
  )

  trait NameserverApi {

    def applyChangeSet(domainName: DomainName, upserts: Iterable[Record], deletes: Iterable[Record]): T[Unit]

    def query(domain: DomainName): T[QueryResults]

    def runSync(request: SyncRequest) = {
      import request._
      query(domainName).flatMap { queryResults =>
        val deletes =
          previousState
            .flatMap { previousRecord =>
              if ( newState.exists(_.name === previousRecord.name) ) {
                None
              } else {
                previousRecord.some
              }
            }

        applyChangeSet(domainName, newState, deletes)

      }
    }

  }

}
