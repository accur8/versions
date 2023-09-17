package a8.orbitjirasync


import a8.common.logging.LoggingF
import a8.shared.{CompanionGen, ConfigMojo, LongValue, StringValue}

import java.util.{Base64, UUID}
import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn.readLine
import a8.shared.jdbcf.SqlString
import a8.shared.jdbcf.mapper.PK
import a8.shared.json.ast.{JsArr, JsBool, JsDoc, JsObj, JsStr, JsVal}
import a8.sync.http.{InvalidHttpResponseStatusCode, Method, Request, RequestProcessor}
import a8.sync.qubes.{QubesAnno, QubesApiClient}
import a8.sync.qubes.QubesApiClient.QueryRequest
import sttp.model.Uri._
import a8.shared.jdbcf.SqlString._
import a8.sync.http
import a8.orbitjirasync.model._
import sttp.model.Uri
import zio.stream.{ZSink, ZStream}

import java.time.LocalDateTime
import java.util
import a8.shared.SharedImports._
import zio.ZIO

case class OrbitTaskManager(config: JiraSync, parallelism: Int)(implicit val requestProcessor: RequestProcessor, qubesClient: QubesApiClient) extends LoggingF {

  import a8.Scala3Hacks.*

  type IO[A] = zio.Task[A]

  import OrbitTaskManagerMain.orbit

  lazy val jiraConfig = config.jiraConfig

  def taskActions: XStream[TaskAction] =
    runBoardMappings ++ runJqlMappings

  def runBoardMappings: XStream[TaskAction] =
    ZStream.fromIterable(config.boardMappings)
      .flatMap(jbm =>
        syncJiraBoard(jbm.jiraBoardId, jbm.mappings)
      )

  def runJqlMappings: XStream[TaskAction] =
    ZStream.fromIterable(config.jqlMappings)
      .flatMap { jqlMapping =>
        jqlMappingStream(jqlMapping)
      }

  def jqlMappingStream(mapping: JqlToProjectMapping): XStream[TaskAction] = {
    for {
      issues <- jira.search(mapping.jql).zstreamEval
      tasks <- orbit.retrieveTasks(mapping.orbitProjectUid).zstreamEval
      tasksByKey = tasks.toMapTransform(_.name)
      taskAction <- (
        hideClosedTickets(tasks, issues)
        ++ ZStream.fromIterable(issues)
            .flatMap { issue =>
              tasksByKey.get(issue.key.value) match {
                case None =>
                  ZStream(TaskAction.Insert(issue, mapping.orbitProjectUid, config.jiraConfig.ticketUrlPrefix))
                case Some(task) if !task.visible && issue.status != "Resolved" =>
                  ZStream(TaskAction.Show(issue.key, task.uid))
                case _ =>
                  ZStream.empty
              }
            }
        )
    } yield taskAction
  }

  def syncJiraBoard(jiraBoardId: JiraBoardId, mappings: Vector[EpicToProjectMapping]): XStream[TaskAction] = {
    for {
      jiraBoard <- jira.retrieveJiraBoard(jiraBoardId).zstreamEval
      taskAction <-
        mappings
          .map(mapping => syncEpic(jiraBoard, mapping))
          .reduce(_ ++ _)
    } yield taskAction
  }

  def syncEpic(jiraBoard: JiraBoard, mapping: EpicToProjectMapping): XStream[TaskAction] = {

    val issues: Seq[Issue] = {
      mapping.epicKey match {
        case Some(epicKey) =>
          jiraBoard
            .issuesByEpicKey
            .getOrElse(epicKey, Vector.empty)
        case None =>
          jiraBoard
            .issuesByKey
            .values
            .toVector
      }
    }

    for {
      tasks <- orbit.retrieveTasks(mapping.orbitProjectUid).zstreamEval
      taskAction <- (
        hideClosedTickets(tasks, issues)
        ++
          ZStream.fromIterable(issues)
            .flatMap { issue =>
              tasks.find(_.name === issue.key.value) match {
                case None =>
                  ZStream(TaskAction.Insert(issue, mapping.orbitProjectUid, config.jiraConfig.ticketUrlPrefix))
                case Some(task) if !task.visible && issue.status != "Resolved" =>
                  ZStream(TaskAction.Show(issue.key, task.uid))
                case _ =>
                  ZStream.empty
              }
            }
      )
    } yield taskAction

  }

  def hideClosedTickets(tasks: Vector[Task], issuesAlreadyLoaded: Iterable[Issue]): XStream[TaskAction.Hide] = {
    val thirtyDaysAgo = LocalDateTime.now().minusDays(30)
    val issuesAlreadyLoadedByKey = issuesAlreadyLoaded.toMapTransform(_.key)
    ZStream
      .fromIterable(tasks.filter(_.visible))
      .flatMapPar(parallelism) { task =>
        issuesAlreadyLoadedByKey
          .get(task.asIssueKey)
          .map(i => ZIO.succeed(i.some))
          .getOrElse(jira.retrieveIssue(task.asIssueKey))
          .zstreamEval
          .flatMap {
            case None =>
              ZStream.empty
            case Some(issue) =>
              if (issue.status === "Resolved" && issue.resolutionDate.exists(_.isBefore(thirtyDaysAgo))) {
                ZStream(TaskAction.Hide(issue.key, task.uid))
              } else {
                ZStream.empty
              }
          }
      }
  }

  object jira {

    lazy val authHeaderValue = "Basic " + Base64.getEncoder.encodeToString(s"${jiraConfig.user}:${jiraConfig.password.value}".getBytes)
//    lazy val authHeaderValue = "Basic cmVubnM6OU41WHJPcEA5dzdw"

    def retrieveActiveSprintIds(jiraBoardId: JiraBoardId): IO[Vector[SprintId]] = {
      for {
        response <-
          executeJiraApi { request =>
            request
              .subPath(uri"agile/1.0/board/${jiraBoardId.value}/sprint")
              .addQueryParm("state", "active")
          }
      } yield {
        val json = response.prettyJson
        response("values")
          .unsafeAs[Vector[JsObj]]
          .map(_.apply("id"))
          .map(_.unsafeAs[SprintId])
      }
    }

    def retrieveIssue(issueKey: IssueKey): IO[Option[Issue]] = {
      for {
        response <-
          executeJiraApi { request =>
            request
              .subPath(uri"api/2/issue/${issueKey.value}")
              .addQueryParm("fields", "status,summary,parent,resolutiondate")
          }
            .either
            .flatMap {
              case Right(v) =>
                ZIO.succeed(v.some)
              case Left(InvalidHttpResponseStatusCode(sc, _, _)) if sc.code == 404 =>
                loggerF.debug(s"404 on retrieving ${issueKey}") *>
                  ZIO.succeed(None)
              case Left(th) if th.getMessage.contains("received Some(404)") =>
                loggerF.debug(s"404 on retrieving ${issueKey}") *>
                  ZIO.succeed(None)
              case Left(th) =>
                ZIO.fail(th)
            }
      } yield response.map(issueJsonToIssue)
    }

    def search(jql: String): IO[Vector[Issue]] = {
      for {
        response <-
          executeJiraApi { request =>
            request
              .subPath(uri"api/2/search")
              .addQueryParm("fields", "*all")
              .addQueryParm("jql", jql)
          }
          .flatMap(_("issues").asF[Vector[JsDoc]])
          .map(_.map(issueJsonToIssue))
          .onError(th =>
            loggerF.warn(s"error retrieving -- ${jql}", th)
          )
      } yield response
    }

    def issueJsonToIssue(row: JsDoc): Issue =
      Issue(
        row("key").unsafeAs[IssueKey],
        row("fields")("status")("name").unsafeAs[String],
        row("fields")("summary").unsafeAs[String],
        row("fields")("parent")("key").unsafeAs[Option[IssueKey]],
        row("fields")("resolutiondate").unsafeAs[Option[String]],
      )

    def retrieveJiraBoard(jiraBoardId: JiraBoardId): IO[JiraBoard] =
      for {
        sprintIds <- retrieveActiveSprintIds(jiraBoardId)
        sprints <-
          sprintIds
            .map(retrieveIssuesInSprint(jiraBoardId, _))
            .sequence
      } yield JiraBoard(jiraBoardId, sprints)

    def retrieveIssuesInSprint(jiraBoardId: JiraBoardId, sprintId: SprintId, startAt: Int = 0): IO[Sprint] = {
      val maxResults = 100
      // slightly less poor implementation of pagination
      def impl(actualStartAt: Int): IO[Vector[Issue]] =
        for {
          response <-
            executeJiraApi { request =>
              request
                .subPath(uri"agile/1.0/board/${jiraBoardId.value}/sprint/${sprintId.value}/issue")
                .addQueryParm("maxResults", maxResults.toString)
                .addQueryParm("startAt", actualStartAt.toString)
                .addQueryParm("fields", "status,summary,parent,resolutiondate")
            }
          issues <- {
            val total = response("total").unsafeAs[Int]
            val issues =
              response("issues")
                .unsafeAs[Vector[JsDoc]]
                .map(issueJsonToIssue)
            val nextStartAt = actualStartAt + issues.size
            if (nextStartAt < total) {
              impl(nextStartAt)
                .map(_ ++ issues)
            } else {
              ZIO.succeed(issues)
            }
          }
        } yield issues

      impl(startAt)
        .map(issues => Sprint(sprintId, issues))
    }


    def executeJiraApi(fn: Request => Request): IO[JsDoc] = {
      val request =
        Request(jiraConfig.restApiUri)
          .addHeader("Authorization", authHeaderValue)
          .method(Method.GET)

      val resolvedRequest = fn(request)

      loggerF.debug(s"request with curl -- \n${resolvedRequest.curlCommand}") *>
        resolvedRequest
          .execWithJsonResponse[JsDoc]
    }
  }

}
