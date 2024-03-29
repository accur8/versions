package io.accur8.neodeploy


import io.accur8.neodeploy
import io.accur8.neodeploy.DomainNameSystem.{QueryResults, Record, T}
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, ProfileCredentialsProvider, StaticCredentialsProvider}
import software.amazon.awssdk.services.route53.Route53Client
import software.amazon.awssdk.services.route53.model.{Change, ChangeAction, ChangeBatch, ChangeResourceRecordSetsRequest, ChangeTagsForResourceRequest, HostedZone, ListHostedZonesRequest, ListResourceRecordSetsRequest, ListTagsForResourceRequest, ResourceRecord, ResourceRecordSet, Tag}
import SharedImports.*
import io.accur8.neodeploy.model.{AwsCredentials, DomainName}
import software.amazon.awssdk.regions.Region
import zio.ZIO
import PredefAssist.*

case class AmazonRoute53DnsApi(
  awsCredentials: AwsCredentials,
)
  extends neodeploy.DomainNameSystem.NameserverApi
  with LoggingF
{

  val previousSyncStateKey = "previousSyncState"
  val hostedZoneResourceType = "hostedzone"

  lazy val client =
    Route53Client
      .builder()
      .region(Region.US_EAST_1)
      .credentialsProvider(awsCredentials.asAmazonSdkCredentialsProvider)
      .build()

  def hostedZoneT(domainName: DomainName): T[HostedZone] =
    traceEffect(z"${domainName}") {
      zblock {
        val dottedName = domainName.value.toLowerCase + "."
        val result =
          client
            .listHostedZones(
              ListHostedZonesRequest
                .builder()
                .build()
            )
            .hostedZones()
            .asScala
            .find(_.name().toLowerCase == dottedName)
            .getOrError(s"unable to find ${domainName}")
        result
      }
    }


  override def query(domainName: DomainName): T[QueryResults] = {
    hostedZoneT(domainName)
      .flatMap { hostedZone =>
        val effect =
          ZIO.attemptBlocking {
            val response =
              client
                .listResourceRecordSets(
                  ListResourceRecordSetsRequest
                    .builder()
                    .hostedZoneId(hostedZone.id())
                    .build()
                )

            val currentState =
              response
                .resourceRecordSets()
                .asScala
                .map { rrs =>
                  val values =
                    rrs
                      .resourceRecords()
                      .asScala
                      .map(_.value())
                      .toVector

                  DomainNameSystem.Record(
                    recordType = rrs.typeAsString(),
                    name = DomainName.fromZoneFile(rrs.name()),
                    values = values,
                    ttl = rrs.ttl(),
                  )
                }

            QueryResults(
              domainName = domainName,
              currentState = currentState,
            )

          }
        effect.debugLog(z"query(${domainName})")

      }
  }

  override def applyChangeSet(domainName: DomainName, upserts: Iterable[DomainNameSystem.Record], deletes: Iterable[DomainNameSystem.Record]): T[Unit] =
    traceEffect(z"${domainName}, upserts = ${upserts.mkString("Seq(", ", ", ")")}, deletes = ${deletes.mkString("Seq(", ", ", ")")}") {
      hostedZoneT(domainName).flatMap { hostedZone =>
        zblock {

          def toChangeSet(records: Iterable[DomainNameSystem.Record], changeAction: ChangeAction): Iterable[Change] = {
            records
              .map { r =>
                val rr = {
                  r.values.map { v =>
                    ResourceRecord
                      .builder()
                      .value(v)
                      .build()
                  }
                }
                val rrs =
                  ResourceRecordSet
                    .builder()
                    .name(r.name.asDottedName)
                    .`type`(r.recordType)
                    .ttl(r.ttl)
                    .resourceRecords(rr: _*)
                    .build()
                Change
                  .builder()
                  .action(changeAction)
                  .resourceRecordSet(rrs)
                  .build()
              }
          }

          val changes = (toChangeSet(upserts, ChangeAction.UPSERT) ++ toChangeSet(deletes, ChangeAction.DELETE)).toSeq
          //      val changes = toChangeSet(upserts, ChangeAction.UPSERT).toSeq

          client
            .changeResourceRecordSets(
              ChangeResourceRecordSetsRequest
                .builder()
                .hostedZoneId(hostedZone.id())
                .changeBatch(
                  ChangeBatch
                    .builder()
                    .changes(changes: _*)
                    .build()
                )
                .build()
            )
        }
          .as(())
          .trace0(z"AmazonRoute53.applyChangeSet(${domainName}, ${upserts.toString}, ${deletes.toString})")
      }
    }

}
