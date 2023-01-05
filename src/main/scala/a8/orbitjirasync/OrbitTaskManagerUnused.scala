package a8.orbitjirasync

object OrbitTaskManagerUnused {

  //    def runJql(jql: String): IO[Vector[JqlItem]] = {
  //      for {
  //        response <-
  //          executeJiraApi { request =>
  //            request
  //              .subPath(uri"api/3/search")
  //              .jsonBody(JsObj(Map("jql" -> JsStr(jql))))
  //              .method(http.Method.POST)
  //          }
  //      } yield {
  //        val json = response.prettyJson
  //        response("issues")
  //          .unsafeAs[Vector[JsObj]]
  ////          .map(jso => jso("id").unsafeAs[IssueKey] -> jso)
  ////          .filter()
  //          .map { jso =>
  //            val json = jso.prettyJson
  //            JqlItem(jso("key").unsafeAs[IssueKey], jso("fields")("parent")("key").unsafeAs[Option[IssueKey]])
  //          }
  //      }
  //    }

  //  def run0 =
  //    jira
  //      .retrieveActiveSprintIds(jiraConfig.jiraBoardId)
  //      .flatMap { sprintIds =>
  //        sprintIds
  //          .map(sprintId =>
  //            jira.runJql(s"sprint = ${sprintId.value}")
  //          )
  //          .flatSequence
  //      }
  //      .map { issues =>
  //        issues.foreach(println)
  //      }

  //  def mainMenu: IO[Unit] =
  //    for {
  //      _ <- IO.println("")
  //      _ <- IO.println("Please select operation:")
  //      _ <- IO.println("1 - Insert single Jira ticket into Orbit")
  //      _ <- IO.println("2 - Add all Jira tickets from active sprints into Orbit")
  //      _ <- IO.println("3 - Hide closed Jira tickets in Orbit")
  //      _ <- IO.println("Q - Quit")
  //      line <- IO.readLine
  //      _ <-
  //        {
  //          line match {
  //            case "1" =>
  //              insertSingleTicket
  //            case "2" =>
  //              insertSprintTickets
  //            case "3" =>
  //              hideClosedTickets
  //            case "q" | "Q" =>
  //              IO.unit
  //            case _ =>
  //              for {
  //                _ <- IO.println("Invalid selection")
  //                _ <- mainMenu
  //              } yield ()
  //          }
  //        }
  //    } yield ()
  //
  //  def insertSingleTicket: IO[Unit] = {
  //    for {
  //      _ <- IO.println("")
  //      _ <- IO.println("Enter ticket number:")
  //
  //      key = jiraConfig.issuePrefix + readLine(jiraConfig.issuePrefix)
  //
  //      issue <- jira.retrieveIssue(IssueKey(key))
  //
  //      tasks <- orbit.retrieveTasks
  //
  //      _ <-
  //        {
  //          tasks.find(_.name == key) match {
  //            case Some(task) if task.visible =>
  //              IO.println(s"Orbit task for ${key} already exists")
  //            case Some(task) =>
  //              orbit.updateTaskVisible(task.uid, true) >>
  //                IO.println(s"Updated ${key}: Set visible = true")
  //            case None =>
  //              orbit.insertTask(issue) >>
  //                IO.println(s"Created task for ${key}")
  //          }
  //        }
  //      _ <- mainMenu
  //    } yield ()
  //  }

}
