package a8.appinstaller


import a8.shared.{CompanionGen, RuntimePlatform}
import a8.shared.FileSystem.{Directory, File}
import a8.shared.app.Logging

import java.io.ByteArrayInputStream
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.{Files, Paths}
import org.apache.tools.ant.DirectoryScanner
import predef._
import a8.shared.SharedImports._
import a8.shared.HoconOps._
import MxJarMetadata._

object JarMetadata extends MxJarMetadata with Logging {

  import a8.Scala3Hacks.*

  def process(appDir: Directory, jarFile: File): Unit = {
    ZipAssist.readEntryFromZipFile(jarFile, "META-INF/a8-deployer.json").foreach { json =>
      try {
        val jv = parseHocon(json)
        jv.read[JarMetadata].apply(appDir)
      } catch {
        case _: java.nio.file.NoSuchFileException =>
          ()
        case e: Exception =>
          logger.error(s"unable to parse JarMetadata from jar file ${jarFile} -- \n${json}", e)
      }
    }
  }

  object Explode extends MxExplode
  @CompanionGen
  case class Explode(
    jar: String,
    includes: Option[String]
  )

  object SymLink extends MxSymLink
  @CompanionGen
  case class SymLink(
    link: String,
    target: String
  )

}


@CompanionGen
case class JarMetadata(
  explode: List[JarMetadata.Explode],
  chmod_exec: List[String],
  symlinks: List[JarMetadata.SymLink] = Nil,
) extends Logging {

  implicit class ListProcessor[A](l: List[A]) {
    def foreachx(fn: A=>Unit) = {
      l.foreach { i =>
        try {
          logger.debug(s"processing ${i}")
          fn(i)
        } catch {
          case e: Exception =>
            logger.warn(s"error processing ${i}", e)
        }
      }
    }
  }

  def apply(appDir: Directory) = {

    val libDir = appDir \\ "lib"
    val metaInfDir = appDir \\ "META-INF"

    def exploder() = {
      explode.foreachx { x =>
        val scanner = new DirectoryScanner
        scanner.setIncludes(Array(x.jar))
        scanner.setBasedir(libDir.canonicalPath)
        scanner.setCaseSensitive(false)
        scanner.scan()
        scanner.getIncludedFiles.foreach { fs =>
          val f = libDir \ fs
          logger.debug("exploding " + f.canonicalPath)
          ZipAssist.unzip(f, appDir, x.includes)
        }
      }
    }

    def chmoder() = if (!RuntimePlatform.isWindows) {
      chmod_exec.foreachx { x =>
        val scanner = new DirectoryScanner
        scanner.setIncludes(Array(x))
        scanner.setBasedir(appDir.canonicalPath)
        scanner.setCaseSensitive(false)
        scanner.scan()
        scanner.getIncludedFiles.foreach { fs =>
          val f = appDir \ fs
          val path = Paths.get(f.canonicalPath)
          val perms = Files.getPosixFilePermissions(path)
          perms.add(PosixFilePermission.OWNER_EXECUTE)
          perms.add(PosixFilePermission.GROUP_EXECUTE)
          Files.setPosixFilePermissions(path, perms)
        }
      }
    }

    def symlinker() = {
      symlinks.foreachx { symlink =>
        val target = Paths.get(symlink.target)
        val link = Paths.get((appDir \ symlink.link).canonicalPath)

        Files.createSymbolicLink(link, target)
      }
    }

    exploder()
    chmoder()
    symlinker()

    metaInfDir.deleteChildren()
    metaInfDir.delete()

  }

}
