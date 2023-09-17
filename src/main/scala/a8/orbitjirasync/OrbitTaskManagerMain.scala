package a8.orbitjirasync

import a8.shared.app.{BootstrappedIOApp}
import a8.sync.http.RequestProcessor
import a8.sync.qubes.QubesApiClient
import a8.orbitjirasync.model._
import sttp.model.Uri
import zio.stream.{ZSink, ZStream}

import java.util.UUID
import a8.shared.jdbcf.SqlString._
import a8.shared.SharedImports._
import a8.shared.app.BootstrappedIOApp.BootstrapEnv
import zio.ZIO

object OrbitTaskManagerMain extends BootstrappedIOApp {

  type IO[A] = zio.Task[A]


  override def runT: ZIO[BootstrapEnv, Throwable, Unit] =
    for {
      config <- appConfig[model.Config]
      _ <- OrbitTaskManagerImpl(config).runner
    } yield ()

  object orbit {

    def retrieveTasks(orbitProjectUid: OrbitProjectUid)(implicit qubesApiClient: QubesApiClient): IO[Vector[Task]] =
      qubesApiClient
        .query[Task](sql"projectUid = ${orbitProjectUid.value.escape} and jiraTicket is not null")
        .map(_.toVector)

    def insertTask(issue: Issue, orbitProjectUid: OrbitProjectUid, jiraTicketUrlPrefix: Uri)(implicit qubesApiClient: QubesApiClient): IO[Task] =
      qubesApiClient
        .insert(
          Task(
            uid = UUID.randomUUID().toString.replace("-", ""),
            projectUid = orbitProjectUid.some,
            name = issue.key.value,
            description = issue.summary,
            visible = true,
            jiraTicket = jiraTicketUrlPrefix.addPath(issue.key.value).toString.some,
          )
        )


    def updateTaskVisible(uid: String, visible: Boolean)(implicit qubesApiClient: QubesApiClient): IO[Task] =
      for {
        task <- qubesApiClient.fetch[Task, String](uid)
        updatedTask <- qubesApiClient.update(task.copy(visible = visible))
      } yield updatedTask

  }

}


case class OrbitTaskManagerImpl(config: model.Config) extends LoggingF {

  type IO[A] = zio.Task[A]

  val parallelism = 10

  import OrbitTaskManagerMain.orbit

  def logActionsConsumer(stream: XStream[TaskAction], context: String): IO[Unit] =
    stream
      .run(ZSink.collectAll)
      .flatMap { taskActions =>
        if ( taskActions.isEmpty ) {
          loggerF.info("\n\n    no actions needed")
        } else {
          loggerF.info(s"${context} \n${taskActions.mkString("\n").indent("        ")}")
        }
      }

  def readOnlyConsumer(stream: XStream[TaskAction]): IO[Unit] =
    logActionsConsumer(stream, "\n    the following actions are needed:")

  def reallyDoItConsumer(stream: XStream[TaskAction])(implicit qubesApiClient: QubesApiClient): IO[Unit] = {
    val fireTheRocketStream =
      stream
        .mapZIOPar(parallelism) { taskAction =>
          runReallyDoItTaskAction(taskAction)
            .as(taskAction)
        }
    logActionsConsumer(
      fireTheRocketStream,
      context = "\n    performed the following actions:",
    )
  }


  def runReallyDoItTaskAction(taskAction: TaskAction)(implicit qubesApiClient: QubesApiClient): IO[Unit] =
    taskAction match {
      case TaskAction.Show(issueKey, taskUid) =>
        orbit.updateTaskVisible(taskUid, true).as(())
      case TaskAction.Hide(issueKey, taskUid) =>
        orbit.updateTaskVisible(taskUid, false).as(())
      case TaskAction.Insert(issue, orbitProjectUid, jiraTicketUrlPrefix) =>
        orbit.insertTask(issue, orbitProjectUid, jiraTicketUrlPrefix).as(())
    }

  def runner: IO[Unit] = {
    QubesApiClient.asResource(config.qubes).use { implicit qubesApiClient =>
      RequestProcessor.asResource().use { implicit requestProcessor =>

        val consumer =
          if ( config.readOnly ) {
            readOnlyConsumer(_)
          } else {
            reallyDoItConsumer(_)
          }

        val stream =
          ZStream.fromIterable(config.jiraSyncs)
            .flatMap(jiraSync =>
              OrbitTaskManager(jiraSync, parallelism).taskActions
            )
        consumer(stream)
          .logVoid
      }
    }
  }

}