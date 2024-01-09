package io.accur8.neodeploy


import a8.shared.{AbstractStringValueCompanion, ZFileSystem, ZString}
import io.accur8.neodeploy.model.LocalRootDirectory
import SharedImports.*
import a8.shared.ZFileSystem.{Directory, dir}
import a8.shared.ZString.ZStringer
import io.accur8.neodeploy.systemstate.SystemStateModel.PathLocator
import zio.ZIO

object VFileSystem {

  lazy val userHome: Directory = dir(System.getProperty("user.home"))

  import a8.shared.ZFileSystem.SymlinkHandlerDefaults.follow

  def file(path: String): File = new impl.FileImpl(PathName(path))
  def dir(path: String): Directory = new impl.DirectoryImpl(PathName(path))
  def link(path: String): Symlink = new impl.SymlinkImpl(PathName(path))

  object PathName {
    def apply(name: String): PathName =
      new impl.PathNameImpl(impl.scrubPathName(name))
    def unapply(pathName: PathName): Option[String] =
      Some(pathName.name)
  }
  sealed trait PathName {
    val parts: Iterable[String]
    def name: String
    def absPath: String = "/" + path
    def path: String
    def parent: PathName = parentOpt.getOrError(s"${absPath} has no parent")
    def parentOpt: Option[PathName]
    def subPath(subPath: PathName): PathName
    def subPath(subPathStr: String): PathName
    def relativeTo(root: PathName): PathName
    override def toString: String = absPath
  }

  trait Path {
    val pathName: PathName
    def parentOpt: Option[Directory] = pathName.parentOpt.map(new impl.DirectoryImpl(_))
    def zpath: N[ZFileSystem.Path]
    def name: String = pathName.name
    def path: String = pathName.path
    def absPath: String = pathName.absPath
    def exists: N[Boolean]
    override def toString = absPath
  }

  object File extends CustomStringValueCompanion[File] {
    override def valueToString(file: File): String = file.path
    override def valueFromString(path: String): File = new impl.FileImpl(PathName(path))
  }
  trait File extends Path {
    def parent: Directory = new impl.DirectoryImpl(pathName.parent)
    def write(contents: String) = zfile.flatMap(_.write(contents))
    lazy val zfile = zservice[PathLocator].map(_.file(pathName))
    lazy val readAsStringOpt: N[Option[String]] = zfile.flatMap(_.readAsStringOpt)
    lazy val readAsString: N[String] = zfile.flatMap(_.readAsString)
    def delete: N[Unit] = zfile.flatMap(_.delete)
    override def exists: N[Boolean] = zfile.flatMap(_.exists)

    def copyTo(targetFile: File): N[Unit] =
      zfile.zip(targetFile.zfile).flatMap(t => t._1.copyTo(t._2))

    def relativeTo(root: Directory): PathName =
      pathName.relativeTo(root.pathName)

    def renameTo(targetFile: File): N[Unit] =
      zfile
        .zip(targetFile.zfile)
        .flatMap { (sourcez, targetz) =>
          val effect =
            zblock {
              sourcez
                .asNioPath
                .toFile
                .renameTo(targetz.asNioPath.toFile)
            }
          effect.flatMap {
            case true =>
              zunit
            case false =>
              zfail(new RuntimeException(s"rename ${sourcez.absolutePath} --> ${targetz.absolutePath} failed"))
          }
        }

    def toZFile(implicit pathLocator: PathLocator): ZFileSystem.File

  }

  object Directory extends CustomStringValueCompanion[Directory] {
    override def valueToString(d: Directory): String = d.path
    override def valueFromString(path: String): Directory = new impl.DirectoryImpl(PathName(path))
  }
  trait Directory extends Path {
    def resolve = zdir.flatMap(_.makeDirectories).as(this)
    def delete = zdir.flatMap(_.delete)
    def deleteChildren = zdir.flatMap(_.deleteChildren)
    def makeDirectories = zdir.flatMap(_.makeDirectories).as(this)
    def file(path: PathName): File = new impl.FileImpl(pathName.subPath(path))
    def file(path: String): File = new impl.FileImpl(pathName.subPath(path))
    def subdir(path: PathName): Directory = new impl.DirectoryImpl(pathName.subPath(path))
    def subdir(path: String): Directory = new impl.DirectoryImpl(pathName.subPath(path))
    def symlink(path: String): Symlink = new impl.SymlinkImpl(pathName.subPath(path))
    lazy val zdir = zservice[PathLocator].map(_.dir(pathName))
    override def exists: N[Boolean] = zdir.flatMap(_.exists)
    def existsAsDirectory: N[Boolean] = zdir.flatMap(_.existsAsDirectory)

    def toZDir(implicit pathLocator: PathLocator): ZFileSystem.Directory

    def entries: N[Iterable[Path]] =
      zdir
        .flatMap(_.entries)
        .map(entries =>
          entries
            .map {
              case d: ZFileSystem.Directory =>
                subdir(d.name)
              case f: ZFileSystem.File =>
                file(f.name)
              case l: ZFileSystem.Symlink =>
                symlink(l.name)
            }
        )

    def subdirs: N[Iterable[Directory]] =
      entries
        .map(_.collect { case d: Directory => d } )

    def relativeTo(root: Directory): PathName =
      pathName.relativeTo(root.pathName)

    def copyTo(targetDir: Directory): N[Unit] =
      for {
        targetSubdir <- targetDir.subdir(name).makeDirectories
        _ <- copyChildrenTo(targetSubdir)
      } yield ()

    def copyChildrenTo(targetDir: Directory): N[Unit] =
      for {
        children <- entries
        _ <-
          children.map {
            case f: File =>
              f.copyTo(targetDir.file(f.name))
            case d: Directory =>
              d.copyTo(targetDir)
            case l: Symlink =>
              ???
//              l.writeTarget(targetDir.subdir(l.name))
          }.sequence
      } yield ()
//      zfile.zip(targetFile.zfile).flatMap(t => t._1.copyTo(t._2))

  }

  object Symlink extends AbstractStringValueCompanion[Symlink] {
    override def valueToString(s: Symlink): String = s.path
    override def valueFromString(path: String): Symlink = new impl.SymlinkImpl(PathName(path))
  }
  trait Symlink extends Path {
    def deleteIfExists: N[Unit] = zlink.flatMap(_.deleteIfExists)
    def asDirectory: Directory = new impl.DirectoryImpl(pathName)
    lazy val zlink = zservice[PathLocator].map(_.link(pathName))
    def writeTarget(target: Path): N[Unit] = zlink.zip(target.zpath).flatMap(t => t._1.writeTarget(t._2.absolutePath))
    def delete: N[Unit] = zlink.flatMap(_.delete)
    override def exists: N[Boolean] = zlink.flatMap(_.exists)
  }

  object impl {

    def scrubPathName(rawPath: String): List[String] = {
      rawPath
        .split('/')
        .map(_.trim)
        .filter(_.length > 0)
        .filterNot(p => p == "." || p == "..")
        .toList
    }

    class FileImpl(val pathName: PathName) extends File {
      override def zpath: N[ZFileSystem.Path] = zfile
      override def toZFile(implicit pathLocator: PathLocator): ZFileSystem.File = pathLocator.file(pathName)
    }
    class DirectoryImpl(val pathName: PathName) extends Directory {
      override def zpath: N[ZFileSystem.Path] = zdir
      override def toZDir(implicit pathLocator: PathLocator): ZFileSystem.Directory = pathLocator.dir(pathName)
    }

    class SymlinkImpl(val pathName: PathName) extends Symlink {
      override def zpath: N[ZFileSystem.Path] = zlink
    }

    case class PathNameImpl(parts: Iterable[String]) extends PathName {
      override def parentOpt: Option[PathName] =
        parts.dropRight(1) match {
          case p if p.isEmpty =>
            None
          case parentParts =>
            new PathNameImpl(parentParts).some
        }
      override def subPath(subPathStr: String): PathName = subPath(PathName(subPathStr))
      lazy val path = parts.mkString("/")
      lazy val name = parts.last

      override def subPath(subPath: PathName): PathName =
        PathNameImpl(parts ++ subPath.parts)

      override def relativeTo(root: PathName): PathName = {
        val matchingPartCount =
          root
            .parts
            .zip(parts)
            .takeWhile(t => t._1 == t._2)
            .size
        PathNameImpl(parts.drop(matchingPartCount))
      }
    }

  }

}
