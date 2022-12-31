package io.accur8.neodeploy


import a8.shared.ZFileSystem.Directory
import a8.shared.json.JsonCodec
import a8.shared.SharedImports.{zservice, _}
import a8.shared.{StringValue, ZFileSystem}
import a8.shared.ZString.ZStringer
import a8.shared.app.{Logging, LoggingF}
import a8.shared.jdbcf.ISeriesDialect.logger
import a8.shared.json.ast.{JsDoc, JsVal}
import io.accur8.neodeploy.PushRemoteSyncSubCommand.Filter
import io.accur8.neodeploy.Sync.SyncName
import io.accur8.neodeploy.SyncContainer.Prefix
import io.accur8.neodeploy.model.ApplicationName
import io.accur8.neodeploy.systemstate.{Interpreter, SystemState}
import io.accur8.neodeploy.systemstate.SystemStateModel._
import zio.prelude.Equal
import zio.{Task, UIO, ZIO}
import PredefAssist._

object SyncContainer extends LoggingF {

  case class Prefix(value: String)

  def loadState(stateDirectory: ZFileSystem.Directory, prefix: Prefix): Task[Vector[PreviousState]] = {
    loggerF.debug(s"loading state ${stateDirectory}  prefix = ${prefix}") *>
    stateDirectory
      .files
      .flatMap { files =>
        val effect: Vector[UIO[Option[PreviousState]]] =
          files
            .filter(f => f.name.startsWith(prefix.value) && f.name.endsWith(".json"))
            .toVector
            .map(file =>
              json.fromFile[PreviousState](file)
                .either
                .flatMap {
                  case Left(e) =>
                    loggerF.warn("error loading previous state", e)
                      .as(None)
                  case Right(ps) =>
                    loggerF.debug(s"loaded previous state -- \n${ps.prettyJson}")
                      .as(ps.some)
                }
              )
        ZIO.collectAll(effect)
          .map(_.flatten)
      }
  }
}

abstract class SyncContainer[Resolved, Name <: StringValue : Equal](
  prefix: Prefix,
  stateDirectory: Directory,
  filter: Filter[Name],
)
  extends LoggingF
{

  val previousStates: Vector[PreviousState]
  val newResolveds: Vector[Resolved]
  val staticSyncs: Seq[Sync[Resolved]]
  def resolvedSyncs(resolved: Resolved): Seq[Sync[Resolved]]

  def syncs(resolved: Option[Resolved]) =
    staticSyncs ++ resolved.toSeq.flatMap(resolvedSyncs)

  lazy val newResolvedsByName: Map[Name,Resolved] =
    newResolveds
      .map(r => name(r) -> r)
      .toMap

  def name(resolved: Resolved): Name
  def nameFromStr(nameStr: String): Name

  case class NamePair(syncName: SyncName, resolvedName: Name)

  lazy val previousStatesByNamePair: Map[NamePair, PreviousState] =
    previousStates
      .map(s => NamePair(s.syncName, nameFromStr(s.resolvedName)) -> s)
      .toMap

  lazy val allNamePairs: Vector[NamePair] = {

    val currentNamePairs: Vector[NamePair] =
      newResolveds.flatMap(resolved =>
        syncs(resolved.some).map(sync =>
          NamePair(sync.name, name(resolved))
        )
      )

    val result =
      (previousStatesByNamePair.keySet.toVector ++ currentNamePairs)
        .distinct

//    logger.debug(s"allNamePairs = ${result}")

    result

  }

  def run: ZIO[Environ, Nothing, Either[Throwable,Unit]] =
    loggerF.debug(s"running allNamePairs = ${allNamePairs}") *>
    allNamePairs
      .map { pair =>
        val previousState: PreviousState =
          previousStatesByNamePair
            .get(pair)
            .getOrElse(PreviousState(ResolvedState(pair.resolvedName.value, pair.  syncName, SystemState.Empty)))
        run(pair, previousState)
      }
      .sequence
      .as(())
      .either

  def run(namePair: NamePair, previousState: PreviousState): M[Unit] = {

    val resolvedOpt = newResolveds.find(r => name(r) === namePair.resolvedName)
    val syncOpt = syncs(resolvedOpt).find(_.name === namePair.syncName)

    val newStateEffect =
      (
        (syncOpt, resolvedOpt) match {
          case (Some(sync), Some(resolved)) =>
            traceLog(
              s"systemState(${namePair})",
              sync.systemState(resolved)
            )
          case _ =>
            zsucceed(SystemState.Empty)
        }
      ).map(s => NewState(ResolvedState(namePair.resolvedName.value, namePair.syncName, s)))

    val effect: M[Unit] =
      for {
        _ <- loggerF.trace(s"starting run(${namePair})")
        newState <- newStateEffect
        _ <- loggerF.trace(s"new state calculated ${namePair}")
        interpretter <- systemstate.Interpreter(newState, previousState)
        _ <- loggerF.trace(s"interpreter created ${namePair}")
        _ <- interpretter.dryRunLog.map(m => loggerF.info(m)).getOrElse(zunit)
        _ <- loggerF.trace(s"applying new start ${namePair}")
        _ <- interpretter.runApplyNewState
        _ <- loggerF.trace(s"uninstalling obsolete ${namePair}")
        _ <- interpretter.runUninstallObsolete
        _ <- loggerF.trace(s"updating state ${namePair}")
        _ <- runSystemStateServicesCommit
        _ <- updateState(newState)
      } yield ()

    effect
      .either
      .flatMap {
        case Right(_) =>
          zunit
        case Left(th) =>
          loggerF.error(s"error processing ${namePair}", th)
      }
      .correlateWith(s"SyncContainer.run(${namePair})")

  }

  def runSystemStateServicesCommit: M[Unit] = zunit

  def updateState(newState: NewState): Task[Unit] = {
    val isEmpty = newState.isEmpty
    val stateFile = stateDirectory.file(z"${prefix.value}-${newState.resolvedName}-${newState.syncName}.json")
    if (isEmpty) {
      for {
        exists <- stateFile.exists
        _ <-
          if (exists) {
            loggerF.debug(z"deleting state ${stateFile}") *>
              stateFile.delete
          } else {
            zunit
          }
      } yield ()
    } else {
      loggerF.debug(z"updating state ${stateFile}") *>
        stateFile.write(newState.prettyJson)
    }
  }


}
