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
  deploys: Vector[DeployArg],
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
          loggerF.warn("error loading previous state", e)
            .as(PreviousState(ResolvedState(deployId, SystemState.Empty)))
        case Right(ps) =>
          loggerF.debug(s"loaded previous state -- \n${ps.prettyJson}")
            .as(ps)
      }


  def run: M[Unit] =
    loggerF.debug(s"running = ${deploys.map(_.deployId).mkString(" ")}") *>
      deploys
        .map { deployArg =>
          for {
            previousState <- loadState(deployArg.deployId)
            _ <- run(deployArg, previousState)
          } yield ()
        }
        .sequence
        .as(())

  def run(deployArg: DeployArg, previousState: PreviousState): M[Unit] = {

    val namePair = deployArg.deployId

    val newStateEffect: M[NewState] =
      deployArg
        .systemState(deployUser)
        .traceLog(s"systemState(${namePair})")
        .map(s => NewState(ResolvedState(namePair, s)))

    val effect: M[Unit] =
      for {
        _ <- loggerF.trace(s"starting run(${namePair})")
        newState <- newStateEffect
        _ <- loggerF.trace(s"new state calculated ${namePair}")
        interpreter <- systemstate.Interpreter(newState, previousState)
        _ <- loggerF.trace(s"interpreter created ${namePair}")
        _ <- interpreter.dryRunLog.map(m => loggerF.info(m)).getOrElse(zunit)
        _ <- loggerF.trace(s"applying new state ${namePair}")
        _ <- interpreter.runApplyNewState
        _ <- loggerF.trace(s"applying uninstall if obsolete state ${namePair}")
        _ <- interpreter.runUninstallObsolete
        _ <- loggerF.trace(s"updating state ${namePair}")
        _ <- runSystemStateServicesCommit
        _ <- updateState(newState)
      } yield ()

    effect
      .correlateWith(s"SyncContainer.run(${namePair})")

  }

  def runSystemStateServicesCommit: M[Unit] = zunit

  def updateState(newState: NewState): M[Unit] = {
    val isEmpty = newState.isEmpty
    val stateFile = resolveStateFile(newState.resolvedSyncState.deployId)
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
