package a8.orbitjirasync

import a8.common.logging.LoggingF
import a8.shared.SharedImports._
import a8.shared.jdbcf.{Conn, DatabaseConfig}
import a8.shared.jdbcf.SqlString._
import a8.sync.qubes.QubesApiClient
import a8.orbitjirasync.model._
import zio.ZIO

/**
 * Keeps the billing app's tasks (billing.task in a continuum database) up to date with the Jira tasks
 * orbittaskman keeps in qubes Orbit, until the switch to billing (FEATURE-20261102-billing-milestone-1).
 *
 * After each run's qubes actions, every Jira task of the mapped projects is read back from qubes Orbit
 * and written into billing.task with orbit's uid: a task billing lacks is inserted, and its status
 * follows orbit's visible (active, or inactive when hidden). A database whose billing schema does not
 * exist yet is skipped, so a database configured before its billing schema is loaded starts receiving
 * tasks once it is. Read-only runs log what they would change.
 */
case class BillingMirror(config: Config)(implicit qubesClient: QubesApiClient) extends LoggingF {

  type IO[A] = zio.Task[A]

  import OrbitTaskManagerMain.orbit

  lazy val projectUids: Vector[OrbitProjectUid] =
    config
      .jiraSyncs
      .flatMap(js => js.boardMappings.flatMap(_.mappings.map(_.orbitProjectUid)) ++ js.jqlMappings.map(_.orbitProjectUid))
      .distinct

  def run: IO[Unit] =
    if (config.billingDatabases.isEmpty || projectUids.isEmpty) {
      ZIO.unit
    } else {
      for {
        orbitTasks <- projectUids.map(orbit.retrieveTasks).sequence.map(_.flatten)
        _ <- config.billingDatabases.map(db => mirrorInto(db, orbitTasks)).sequence
      } yield ()
    }

  def statusOf(task: Task): String =
    if (task.visible) "active" else "inactive"

  def mirrorInto(db: DatabaseConfig, orbitTasks: Vector[Task]): IO[Unit] =
    ZIO.scoped {
      Conn.fromNewConnection(db.url, db.user, db.password.value).flatMap { conn =>
        conn.query[Boolean](sql"select to_regclass('billing.task') is not null").fetch.flatMap {
          case false =>
            loggerF.info(s"billing mirror ${db.url}: no billing schema yet, skipped")
          case true =>
            val inList = SeparatedSqlString(projectUids.map(_.value.escape), CommaSpace)
            for {
              rows <- conn.query[(String, String)](sql"select uid, status from billing.task where projectuid in (${inList})").select
              billingStatus = rows.toMap
              inserts = orbitTasks.filterNot(t => billingStatus.contains(t.uid))
              updates = orbitTasks.filter(t => billingStatus.get(t.uid).exists(_ != statusOf(t)))
              _ <-
                if (inserts.isEmpty && updates.isEmpty) {
                  loggerF.info(s"billing mirror ${db.url}: up to date (${orbitTasks.size} tasks)")
                } else if (config.readOnly) {
                  loggerF.info(s"billing mirror ${db.url}: would insert ${inserts.map(_.name).mkString(", ")}; would set status on ${updates.map(_.name).mkString(", ")}")
                } else {
                  (inserts.map(t => conn.update(insertSql(t))) ++ updates.map(t => conn.update(statusSql(t))))
                    .sequence
                    .flatMap(_ => loggerF.info(s"billing mirror ${db.url}: inserted ${inserts.map(_.name).mkString(", ")}; set status on ${updates.map(_.name).mkString(", ")}"))
                }
            } yield ()
        }
      }
    }

  def insertSql(t: Task) =
    sql"""insert into billing.task (uid, projectuid, name, description, jiraticket, billable, status)
      values (${t.uid.escape}, ${t.projectUid.map(_.value).getOrElse("").escape}, ${t.name.escape}, ${t.description.escape}, ${t.jiraTicket.map(_.escape).getOrElse(Null)}, true, ${statusOf(t).escape})"""

  def statusSql(t: Task) =
    sql"update billing.task set status = ${statusOf(t).escape} where uid = ${t.uid.escape}"

}
