package a8.orbitjirasync


import a8.common.logging.Logging
import a8.shared.{CompanionGen, LongValue, StringValue}
import a8.shared.jdbcf.mapper.PK
import a8.sync.http.RequestProcessor
import a8.sync.qubes.{QubesAnno, QubesApiClient}
import a8.orbitjirasync.Mxmodel._
import sttp.model.Uri
import zio.stream.{ZSink, ZStream}
import a8.shared.SharedImports._
import a8.shared.jdbcf.DatabaseConfig.Password

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.util.UUID

object model extends Logging {

  object SprintId extends LongValue.Companion[SprintId]
  case class SprintId(value: Long) extends LongValue

  object JiraBoardId extends LongValue.Companion[JiraBoardId]
  case class JiraBoardId(value: Long) extends LongValue

  object IssueKey extends StringValue.Companion[IssueKey]
  case class IssueKey(value: String) extends StringValue

  object OrbitProjectUid extends StringValue.Companion[OrbitProjectUid]
  case class OrbitProjectUid(value: String) extends StringValue


  object Issue {

    object impl {
      val resolutionDateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSZ")
    }

    def parseResolutionDateStr(resolutionDateStrOpt: Option[String]): Option[LocalDateTime] =
      resolutionDateStrOpt
        .flatMap { resolutionDateStr =>
          try {
            Some(LocalDateTime.parse(resolutionDateStr, impl.resolutionDateFormat))
          } catch {
            case e: DateTimeParseException =>
              logger.warn(s"swallowing error parsing ${resolutionDateStrOpt}", e)
              None
          }
        }

  }
  /**
   * @param resolutionDateStr format is "2020-08-11T12:44:59.728-0400"
   */
  case class Issue(key: IssueKey, status: String, summary: String, parent: Option[IssueKey], resolutionDateStr: Option[String]) {

    def epicKey(jiraBoard: JiraBoard): IssueKey =
      parent match {
        case None =>
          key
        case Some(pk) =>
          jiraBoard
            .issuesByKey
            .get(pk)
            .map(_.epicKey(jiraBoard))
            .getOrElse(pk)
      }

    lazy val resolutionDate = Issue.parseResolutionDateStr(resolutionDateStr)

  }

  case class Sprint(id: SprintId, issues: Vector[Issue]) {
    def +(right: Sprint) =
      copy(issues = issues ++ right.issues)
  }

  case class JiraBoard(id: JiraBoardId, sprints: Vector[Sprint]) {

    lazy val issuesByKey = {
      sprints
        .flatMap(_.issues)
        .toMapTransform(_.key)
    }

    lazy val issuesByEpicKey = {
      sprints
        .flatMap(_.issues)
        .groupBy(_.epicKey(this))
    }

  }

  object Task extends MxTask
  @CompanionGen(qubesMapper = true)
  @QubesAnno(appSpace = "reaper")
  case class Task(
    @PK uid: String,
    name: String,
    description: String,
    visible: Boolean,
    jiraTicket: Option[String] = None,
    projectUid: Option[OrbitProjectUid] = None,
  ) {
    val asIssueKey = IssueKey(name)
  }

  object Config extends MxConfig
  @CompanionGen
  case class Config(
    jiraSyncs: Vector[JiraSync],
    qubes: QubesApiClient.Config,
    readOnly: Boolean = true,
  )

  object JiraSync extends MxJiraSync
  @CompanionGen
  case class JiraSync(
    jiraConfig: JiraConfig,
    boardMappings: Vector[JiraBoardMapping] = Vector.empty,
    jqlMappings: Vector[JqlToProjectMapping] = Vector.empty,
  )


  object JiraConfig extends MxJiraConfig
  @CompanionGen
  case class JiraConfig(
    serverRootUrl: Uri,
    user: String,
    password: Password,
  ) {
    lazy val restApiUri = serverRootUrl.addPath("rest")
    lazy val ticketUrlPrefix = unsafeParseUri(serverRootUrl.addPath("browse").toString() + "/")
  }

  object EpicToProjectMapping extends MxEpicToProjectMapping
  @CompanionGen
  case class EpicToProjectMapping(
    epicKey: Option[IssueKey] = None,
    orbitProjectUid: OrbitProjectUid,
    description: Option[String] = None,
  )

  object JiraBoardMapping extends MxJiraBoardMapping
  @CompanionGen
  case class JiraBoardMapping(
    jiraBoardId: JiraBoardId,
    mappings: Vector[EpicToProjectMapping],
  )

  object JqlToProjectMapping extends MxJqlToProjectMapping
  @CompanionGen
  case class JqlToProjectMapping(
    jql: String,
    orbitProjectUid: OrbitProjectUid,
    description: Option[String] = None,
  )

  sealed trait TaskAction {
    val issueKey: IssueKey
  }

  object TaskAction {
    case class Show(issueKey: IssueKey, taskUid: String) extends TaskAction
    case class Hide(issueKey: IssueKey, taskUid: String) extends TaskAction
    //    case class Update(issueKey: IssueKey, task: Task) extends TaskAction
    case class Insert(issue: Issue, orbitProjectUid: OrbitProjectUid, jiraTicketUrlPrefix: Uri) extends TaskAction {
      val issueKey = issue.key
    }
  }

}
