package io.accur8.neodeploy.systemstate


import io.accur8.neodeploy.systemstate.SystemStateModel._
import a8.shared.SharedImports._

trait SymlinkMixin extends SystemStateMixin { self: SystemState.Symlink =>

  import a8.shared.ZFileSystem.SymlinkHandlerDefaults.noFollow

  override def stateKey: Option[StateKey] =
    Some(StateKey("symlnk", link.path))

  override def dryRunInstall: Vector[String] =
    Vector(s"symlink ${link} -> ${target}")

  override def dryRunUninstall(interpreter: Interpreter): Vector[String] =
    dryRunInstall.map("uninstall " + _)

  override def isActionNeeded: M[Boolean] =
    link
      .exists
      .flatMap {
        case true =>
          link
            .readTarget
            .map(_ != target)
        case false =>
          zsucceed(true)
      }

  /**
   * applies the state for just this system state and no sub states
   */
  override def runApplyNewState: ApplyState[Unit] =
    for {
      _ <- link.parent.makeDirectories
      _ <- link.writeTarget(target)
    } yield ()

  /**
   * uninstalls the state for just this system state and no sub states
   */
  override def runUninstallObsolete(interpreter: Interpreter): ApplyState[Unit] =
    link
      .exists
      .flatMap {
        case true =>
          link.delete
        case false =>
          zunit
      }

}
