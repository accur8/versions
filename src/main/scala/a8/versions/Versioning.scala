package a8.versions


import a8.common.logging.Logging

import java.io.*
import java.net.URL
import java.util
import java.util.Properties
import a8.shared.SharedImports.*
import a8.versions.Versioning.Entry

import java.nio.ByteBuffer
import scala.collection.concurrent.TrieMap

object Versioning extends Logging {

  lazy val VersioningFile: String = "META-INF/version.properties"
  lazy val VersioningDetailFile: String = "META-INF/version-details.properties"
  lazy val INDENT: String = "   "
  lazy val versionCache = TrieMap.empty[Class[_], Versioning]

  def getAllVersioningResources(classLoader: ClassLoader): List[Versioning] =
    classLoader
      .getResources(VersioningFile)
      .asIterator()
      .asScala
      .toList
      .flatMap { url =>
        try {
          Some(impl.fromUrl(url))
        } catch {
          case e: Exception =>
            logger.warn("unable to load versioning info from " + url, e)
            None
        }
      }

  def apply(clazz: Class[_]): Versioning = {
    versionCache
      .getOrElseUpdate(
        clazz,
        impl.fromClass(clazz),
      )
  }

  object Entry {

    def load(url: URL): Option[Entry] =
      try {

        val props = new Properties()
        val in = url.openStream
        val allBytes = in.readAllBytes()
        props.load(new ByteArrayInputStream(allBytes))
        in.close()

        val contents = new String(allBytes, Utf8Charset)

        Some(Entry(
          url,
          props.asScala.toMap,
          contents
        ))

      } catch {
        case e: Exception =>
          Versioning.logger.debug("error loading " + url, e)
          None
      }

  }

  case class Entry(
    classpathUrl: URL,
    properties: Map[String,String],
    contents: String,
  )

  object impl {

    def fromUrl(url: URL): Versioning = {
      val entries = Entry.load(url) ++ Entry.load(new URL(url.toExternalForm + VersioningDetailFile))
      Versioning(entries)
    }

    def fromClass(clazz: Class[_]): Versioning = {
      try {
        val classnameUrlPart = clazz.getName.replace('.', '/') + ".class"
        val classUrl = clazz.getClassLoader.getResource(classnameUrlPart).toExternalForm
        val baseUrl = new URL(classUrl.substring(0, classUrl.length - classnameUrlPart.length))
        val url  = new URL(baseUrl.toExternalForm + VersioningFile)
        fromUrl(url)
      } catch {
        case e: Exception =>
          logger.warn("unable to load versioning for " + clazz.getName)
          Versioning(Iterable.empty)
      }
    }

  }

}

case class Versioning(entries: Iterable[Entry]) {
  lazy val properties =
    entries
      .foldLeft(Map[String,String]()) { case (acc, entry) =>
        acc ++ entry.properties
      }
}