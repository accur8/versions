package io.accur8.neodeploy.systemstate


import io.accur8.neodeploy.systemstate.SystemStateModel.*
import io.accur8.neodeploy.SharedImports.*
import a8.shared.ZFileSystem
import io.accur8.neodeploy.VFileSystem.PathName
import zio.ZIO

trait SymlinkMixin extends SystemStateMixin { self: SystemState.Symlink =>

  import a8.shared.ZFileSystem.SymlinkHandlerDefaults.noFollow

  def linkZ = zservice[PathLocator].map(_.link(link.pathName))
  def targetZ = zservice[PathLocator].map(_.file(targetPath))

  override def stateKey: Option[StateKey] =
    Some(StateKey("symlink", link.path))

  override def dryRunInstall: Vector[String] =
    Vector(s"symlink ${link.path} -> ${targetPath}")

  override def dryRunUninstall(interpreter: Interpreter): Vector[String] =
    dryRunInstall.map("uninstall " + _)

  val argsEffect: N[(ZFileSystem.Symlink, Boolean, ZFileSystem.File)] =
    linkZ
      .flatMap(l => l.exists.map(l -> _))
      .zip(targetZ)

  override def isActionNeeded: M[Boolean] = {
    argsEffect
      .flatMap {
        case (link, true, target) =>
          link
            .readTarget
            .map(_ != target.absolutePath)
        case (_, false, _) =>
          zsucceed(true)
      }
  }

  /**
   * applies the state for just this system state and no sub states
   */
  override def runApplyNewState: M[Unit] =
    for {
      rlink <- linkZ
      _ <- rlink.parent.makeDirectories
      rtarget <- targetZ
      _ <- rlink.writeTarget(rtarget.absolutePath)
    } yield ()

  /**
   * uninstalls the state for just this system state and no sub states
   */
  override def runUninstallObsolete(interpreter: Interpreter): M[Unit] =
    argsEffect
      .flatMap {
        case (link, true, _) =>
          link.delete
        case (_, false, _) =>
          zunit
      }

}
