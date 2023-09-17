package a8

import a8.shared.ZFileSystem.SymlinkHandler

object Scala3Hacks {

//  implicit def name: sourcecode.Name = ???
//  implicit def pos: LoggerF.Pos = ???

  implicit val symlinkHandler: SymlinkHandler = SymlinkHandler.Follow

}
