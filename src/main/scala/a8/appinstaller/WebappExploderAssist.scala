package a8.appinstaller

import a8.shared.FileSystem.{Directory, File}
import a8.common.logging.Logging

import java.io.InputStream
import java.io.PrintStream
import java.net.URL
import java.util.jar.JarFile
import a8.versions.WebappExploder

object WebappExploderAssist extends Logging {

  val WebappCompositeFolderName = "webapp-composite"

  private val jarFilePrefix = "jar:file:"
  private val filePrefix = "file:"

  private val WebappPrefix = "webapp/"

  def apply(installDir: Directory, jarFiles: Iterable[File]): Unit = {

    WebappExploder.explodeEntries(
      classpathEntries = jarFiles,
      target = installDir.subdir(WebappCompositeFolderName),
    )

  }

}
