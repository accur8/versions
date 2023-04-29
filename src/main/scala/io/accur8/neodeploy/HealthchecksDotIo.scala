package io.accur8.neodeploy

import a8.shared.{CompanionGen, StringValue}
import a8.shared.SharedImports._
import a8.shared.app.LoggingF
import io.accur8.neodeploy.HealthchecksDotIo.{ApiAuthToken, CheckReadOnly, CheckUpsertRequest, impl}
import io.accur8.neodeploy.MxHealthchecksDotIo._
import zio.{Task, ZIO}

import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.time.Duration

object HealthchecksDotIo {

  object impl {
    val httpClient =
      HttpClient
        .newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(20))
        .build();

    def send(request: HttpRequest): Task[HttpResponse[String]] =
      ZIO.attemptBlocking(
        httpClient.send(request, HttpResponse.BodyHandlers.ofString())
      )

    def unsafeSend(request: HttpRequest): HttpResponse[String] =
      httpClient.send(request, HttpResponse.BodyHandlers.ofString())

  }


  object CheckReadOnly extends MxCheckReadOnly
  @CompanionGen
  case class CheckReadOnly(
    name: String,
    slug: String,
    tags: String,
    desc: String,
    grace: Long,
    n_pings: Long,
    status: String,
    last_ping: Option[String] = None,
    next_ping: Option[String] = None,
    manual_resume: Boolean,
    methods: String,
    success_kw: String,
    failure_kw: String,
    filter_subject: Boolean,
    filter_body: Boolean,
    ping_url: String,
    update_url: String,
    pause_url: String,
    resume_url: String,
    channels: String,
    schedule: Option[String] = None,
    tz: Option[String] = None,
    subject: Option[String] = None,
    subject_fail: Option[String] = None,
    timeout: Option[Long] = None,
    last_duration: Option[Long] = None,
  ) {

    def resolvedTags: Vector[String] =
      tags
        .splitList(" ")
        .toVector

    def matches(upsertRequest: CheckUpsertRequest) = {

      def check[A](actual: A, getter: CheckUpsertRequest=>Option[A]): Boolean = {
        getter(upsertRequest) match {
          case None =>
            true
          case Some(v) =>
            v.equals(actual)
        }
      }

      (
        name === upsertRequest.name
          && check(tags, _.tags)
          && check(desc, _.desc)
          && check(timeout, _.timeout)
          && check(grace, _.grace)
          && check(schedule, _.schedule)
          && check(tz, _.tz)
      )
    }
  }


  /**
    * https://healthchecks.io/docs/api/#create-check
    */
  object CheckUpsertRequest extends MxCheckUpsertRequest
  @CompanionGen
  case class CheckUpsertRequest(
    name: String,
    tags: Option[String] = None,
    desc: Option[String] = None,
    /** the period of this check */
    timeout: Option[Long] = None,
    grace: Option[Long] = None,
    /** cron schedule */
    schedule: Option[String] = None,
    tz: Option[String] = None,
    unique: Iterable[String] = Iterable.empty,
  ) {
    def resolvedTags: Vector[String] =
      tags
        .toVector
        .flatMap(_.splitList(" "))
  }

  object ApiAuthToken extends StringValue.Companion[ApiAuthToken]
  case class ApiAuthToken(value: String) extends StringValue

}

case class HealthchecksDotIo(apiAuthToken: ApiAuthToken) extends LoggingF { self =>

  def baseRequest =
    HttpRequest
      .newBuilder()
      .header("X-Api-Key", apiAuthToken.value)

  def listChecks: Task[Iterable[CheckReadOnly]] = {
    val request =
      baseRequest
        .GET()
        .uri(java.net.URI.create("https://healthchecks.io/api/v1/checks/"))
        .build()

    impl.send(request)
      .flatMap { response =>
        if (response.statusCode() / 100 == 2) {
          val responseJsv = json.unsafeParse(response.body())
          responseJsv("checks").asF[Iterable[CheckReadOnly]]
        } else {
          zfail(new RuntimeException(s"Unexpected response from healthchecks.io: ${response.statusCode()} ${response.body()}"))
        }
      }

  }

  def isUpdateNeeded(check: CheckUpsertRequest): Task[Boolean] =
    fetchCheck(check.name)
      .map {
        case None =>
          true
        case Some(actualCheck) if actualCheck.matches(check) =>
          true
        case _ =>
          false
      }

  def fetchCheck(name: String): Task[Option[CheckReadOnly]] =
    listChecks
      .map(_.find(_.name == name))

  def doesCheckExist(name: String): Task[Boolean] =
    fetchCheck(name)
      .map(_.nonEmpty)

  def upsert(check: CheckUpsertRequest): Task[CheckReadOnly] = {
    val jsonRequestBody: HttpRequest.BodyPublisher =
      HttpRequest.BodyPublishers.ofString(check.prettyJson)

    val request =
      baseRequest
        .POST(jsonRequestBody)
        .uri(java.net.URI.create("https://healthchecks.io/api/v1/checks/"))
        .header("Content-Type", "application/json")
        .build()

    impl.send(request)
      .flatMap( response =>
        if ( response.statusCode() / 100 == 2 ) {
          json.readF[CheckReadOnly](response.body())
        } else {
          zfail(new RuntimeException(s"Unexpected response from healthchecks.io: ${response.statusCode()} ${response.body()}"))
        }
      )

  }

  def disable(check: CheckUpsertRequest): Task[Option[CheckReadOnly]] = {
    pause(check.name)
      .flatMap {
        case Some(cro) =>
          val tags = (cro.resolvedTags ++ Vector("disabled")).distinct.mkString(" ")
          upsert(check.copy(tags = Some(tags)))
            .map(_.some)
        case None =>
          zsucceed(None)
      }
  }

  def pause(name: String): Task[Option[CheckReadOnly]] = {
    fetchCheck(name)
      .flatMap {
        case Some(check) =>
          val request =
            baseRequest
              .POST(HttpRequest.BodyPublishers.ofString(""))
              .uri(java.net.URI.create(check.pause_url))
              .build()

          impl.send(request)
            .flatMap( response =>
              if (response.statusCode() / 100 == 2) {
                json.readF[CheckReadOnly](response.body())
                  .map(_.some)
              } else {
                zfail(new RuntimeException(s"Unexpected response from healthchecks.io: ${response.statusCode()} ${response.body()}"))
              }
            )

        case None =>
          zsucceed(None)
      }
  }

}
