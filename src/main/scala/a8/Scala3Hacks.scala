package a8

import a8.shared.ZFileSystem.SymlinkHandler

object Scala3Hacks {

  implicit val symlinkHandler: SymlinkHandler = SymlinkHandler.Follow

}
