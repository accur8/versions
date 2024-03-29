package io.accur8.neodeploy


import io.accur8.neodeploy.VFileSystem
import VFileSystem.Directory
import a8.shared.json.JsonCodec
import a8.shared.SharedImports.{zservice, _}
import a8.shared.{StringValue}
import a8.shared.ZString.ZStringer
import a8.common.logging.{Logging, LoggingF}
import a8.shared.jdbcf.ISeriesDialect.logger
import a8.shared.json.ast.{JsDoc, JsVal}
import io.accur8.neodeploy.model.ApplicationName
import io.accur8.neodeploy.systemstate.{Interpreter, SystemState}
import io.accur8.neodeploy.systemstate.SystemStateModel._
import zio.prelude.Equal
import zio.{Task, UIO, ZIO}
import PredefAssist._

import a8.Scala3Hacks.*

object SyncContainer extends LoggingF {

//  def loadState(stateDirectory: ZFileSystem.Directory): Task[Vector[PreviousState]] = {
//    loggerF.debug(s"loading state ${stateDirectory}") *>
//    stateDirectory
//      .files
//      .flatMap { files =>
//        val effect: Vector[UIO[Option[PreviousState]]] =
//          files
//            .filter(f => f.name.startsWith(prefix.value) && f.name.endsWith(".json"))
//            .toVector
//            .map(file =>
//              json.fromFile[PreviousState](file)
//                .either
//                .flatMap {
//                  case Left(e) =>
//                    loggerF.warn("error loading previous state", e)
//                      .as(None)
//                  case Right(ps) =>
//                    loggerF.debug(s"loaded previous state -- \n${ps.prettyJson}")
//                      .as(ps.some)
//                }
//              )
//        ZIO.collectAll(effect)
//          .map(_.flatten)
//      }
//  }

}

case class SyncContainer(
  stateDirectory: Directory,
  deployUser: DeployUser,
  deployables: Iterable[Deployable],
  dryRun: Boolean,
)
  extends LoggingF
{

  def resolveStateFile(deployId: DeployId): VFileSystem.File =
    stateDirectory.file(z"${deployId.value}.json")

  def loadState(deployId: DeployId): M[PreviousState] =
    resolveStateFile(deployId)
      .zfile
      .flatMap(json.fromFile[PreviousState])
      .either
      .flatMap {
        case Left(e) =>
          loggerF.warn("error loading previous state will continue with previous state of empty", e)
            .as(PreviousState(ResolvedState(deployId, SystemState.Empty)))
        case Right(ps) =>
          loggerF.debug(s"loaded previous state -- \n${ps.prettyJson}")
            .as(ps)
      }


  def run: M[Unit] =
    loggerF.debug(s"syncing ${deployUser} -- ${deployables.map(_.deployId).mkString(" ")}") *>
      deployables
        .map { deployArg =>
          for {
            previousState <- loadState(deployArg.deployId)
            _ <- run(deployArg, previousState)
          } yield ()
        }
        .sequence
        .as(())

  def run(deployable: Deployable, previousState: PreviousState): M[Unit] = {

    lazy val namePair = deployable.deployId

    lazy val newStateEffect: M[NewState] =
      deployable
        .systemState
        .traceLog(s"systemState(${namePair})")
        .map(s => NewState(ResolvedState(namePair, s)))

    lazy val effect: M[Unit] =
      for {
        _ <- loggerF.trace(s"starting run(${namePair})")
        newState <- newStateEffect
        _ <- loggerF.trace(s"new state calculated ${namePair}")
        interpreter <- systemstate.Interpreter(newState, previousState)
        _ <- loggerF.trace(s"interpreter created ${namePair}")
        _ <- interpreter.dryRunLog.map(m => loggerF.info(m)).getOrElse(zunit)
        _ <-
          if (dryRun) {
            loggerF.info(s"dry run only")
          } else {
            makeChangesEffect(newState, interpreter)
          }
      } yield ()

    def makeChangesEffect(newState: NewState, interpreter: systemstate.Interpreter): M[Unit] =
      for {
        _ <- loggerF.trace(s"applying new state ${namePair}")
        _ <- interpreter.runApplyNewState
        dnsService <- zservice[DnsService]
        _ <- dnsService.commit
        _ <- loggerF.trace(s"applying uninstall if obsolete state ${namePair}")
        _ <- interpreter.runUninstallObsolete
        _ <- loggerF.trace(s"updating state ${namePair}")
        _ <- runSystemStateServicesCommit
        _ <- updateState(newState)
      } yield ()

    effect
      .correlateWith(s"SyncContainer.run(${namePair})")

  }

  def runSystemStateServicesCommit: M[Unit] =
    zunit

  def updateState(newState: NewState): M[Unit] = {
    val isEmpty = newState.isEmpty
    val stateFile = resolveStateFile(newState.resolvedSyncState.deployId)
    if (isEmpty) {
      for {
        exists <- stateFile.exists
        _ <-
          if (exists) {
            loggerF.debug(z"deleting state ${stateFile.absPath}") *>
              stateFile.delete
          } else {
            zunit
          }
      } yield ()
    } else {
      loggerF.debug(z"updating state ${stateFile.absPath}") *>
        stateFile.write(newState.prettyJson)
    }
  }


}
