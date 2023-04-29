package io.accur8.neodeploy


import io.accur8.neodeploy.model.{DomainName, ManagedDomain}
import io.accur8.neodeploy.systemstate.SystemState.DnsRecord
import io.accur8.neodeploy.systemstate.SystemStateModel.M
import zio.{Ref, ZLayer}
import a8.shared.SharedImports._
import a8.shared.app.LoggingF
import io.accur8.neodeploy.resolvedmodel.ResolvedRepository

object DnsService extends LoggingF {

  val layer = ZLayer(effect)

  val effect =
    for {
      repo <- zservice[ResolvedRepository]
      ur <- Ref.make(Vector.empty[DnsRecord])
      dr <- Ref.make(Vector.empty[DnsRecord])
      rcr <- Ref.make(Map.empty[DomainName, Iterable[DnsRecord]])
    } yield
      DnsServiceImpl(
        repo.descriptor.managedDomains,
        ur,
        dr,
        rcr,
      )

  case class DnsServiceImpl(
    managedDomains: Iterable[ManagedDomain],
    upsertsRef: Ref[Vector[DnsRecord]],
    deletesRef: Ref[Vector[DnsRecord]],
    recordCacheRef: Ref[Map[DomainName, Iterable[DnsRecord]]],
  ) extends DnsService {

    def records(topLevelDomain: DomainName): M[Option[Iterable[DnsRecord]]] = {
      val effect =
        dnsServiceApi(topLevelDomain) match {
          case None =>
            zsucceed(None)
          case Some(api) =>
            api
              .query(topLevelDomain)
              .map(_.currentState.some)
        }
      recordCacheRef
        .get
        .map(_.get(topLevelDomain))
        .flatMap {
          case None =>
            effect
              .flatMap {
                case None =>
                  zsucceed(None)
                case Some(records) =>
                  recordCacheRef
                    .update(_ + (topLevelDomain -> records))
                    .as(records.some)
              }
          case Some(records) =>
            zsucceed(records.some)
        }
    }

    override def isActionNeeded(record: DnsRecord): M[Boolean] =
      for {
        recordCache <- recordCacheRef.get
        recordsOpt <- records(record.name.topLevelDomain)
        _ <-
          recordsOpt match {
            case None =>
              loggerF.warn(s"no managed domain setup for DnsRecord ${record.compactJson}")
            case Some(_) =>
              zunit
          }
      } yield
        recordsOpt match {
          case None =>
            false
          case Some(records) =>
            !records.exists(_ == record)
        }

    override def upsert(record: DnsRecord): M[Unit] =
      upsertsRef.update(_ :+ record)

    override def delete(record: DnsRecord): M[Unit] =
      deletesRef.update(_ :+ record)

    def dnsServiceApi(tld: DomainName): Option[DomainNameSystem.NameserverApi] =
      managedDomains
        .find(_.topLevelDomains.contains(tld))
        .map(md => AmazonRoute53DnsApi(md.awsCredentials))

    override def commit: M[Unit] =
      for {
        upserts <- upsertsRef.get
        deletes <- deletesRef.get
        upsertsByTld = upserts.groupBy(_.name.topLevelDomain)
        deletesByTld = deletes.groupBy(_.name.topLevelDomain)
        tlds = upsertsByTld.keySet ++ deletesByTld.keySet
        _ <-
          tlds
            .toVector
            .flatMap(tld => dnsServiceApi(tld).map(_ -> tld))
            .map { case (api, tld) =>
              api.applyChangeSet(
                tld,
                upsertsByTld.getOrElse(tld, Vector.empty),
                deletesByTld.getOrElse(tld, Vector.empty),
              )
            }
            .sequencePar
      } yield ()

  }

}

/**
 * a service to collect DnsRecord's so that we can commit that all at once since a lot of dns api's (specifically route 53)
 * work much better when you batch all the changes in a single shot api call
 */
trait DnsService {

  def isActionNeeded(record: DnsRecord): M[Boolean]
  def upsert(record: DnsRecord): M[Unit]
  def delete(record: DnsRecord): M[Unit]
  def commit: M[Unit]

}
