package a8.appinstaller



import a8.shared.FileSystem

import language.postfixOps
import a8.versions.predef._
import a8.shared.SharedImports._
import a8.versions.RepositoryOps
import predef.tryLog

object AppInstaller {

  val standardAppDirectores = Set(
    ".bak",
    "_bak",
    "cache",
    ".cache",
    "config",
    ".config",
    "data",
    ".data",
    "logs",
    ".logs",
    "temp",
    ".temp",
    "tmp",
    ".tmp"
  )
  val excludeFromConfigDirBackup = List("cache", "cache.dir")

}


case class AppInstaller(config: AppInstallerConfig, repositoryOps: RepositoryOps) extends Logging {

  lazy val installBuilder = InstallBuilder(config, repositoryOps)

  lazy val backupDir: FileSystem.Directory = config.resolvedInstallDir \\ "_bak" \\ FileSystem.fileSystemCompatibleTimestamp()

  def execute(): Unit = {

    if (config.backup) {
      backup()
      backupConfigFiles()
    }

    installBuilder.build()

    if ( config.resolveWebappExplode )
      WebappExploderAssist(config.resolvedInstallDir, installBuilder.inventory.classpath.map(FileSystem.file))

    installBuilder.appDir \ "install-inventory.json" write(installBuilder.inventory.prettyJson)

  }


  def backup(): Unit = tryLog(s"backing up app install directory - ${config.resolvedInstallDir.canonicalPath}") {
      backupDir.makeDirectories()
      config
        .resolvedInstallDir
        .entries()
        .filter(e => !AppInstaller.standardAppDirectores.contains(e.name))
        .foreach { p =>
          p.moveTo(backupDir)
        }
  }

  def backupConfigFiles(): Unit = tryLog(s"backing up config files - ${config.resolvedInstallDir.canonicalPath}") {

    List("config", ".config")
      .map(cd => (config.resolvedInstallDir \\ cd))
      .filter(_.exists())
      .foreach { cd =>

        logger.debug(s"backing up ${cd.canonicalPath} except for: ${AppInstaller.excludeFromConfigDirBackup.map(d => s"${cd.name}/${d}/").mkString(", ")}")

        val backupConfigDir = (backupDir \\ cd.name)
        backupConfigDir.makeDirectory()

        cd
          .files()
          .foreach { file =>
            file.copyTo(backupConfigDir)
          }

        cd
          .subdirs()
          .filter(f => !AppInstaller.excludeFromConfigDirBackup.contains(f.name))
          .foreach { dir =>
            val bd = (backupConfigDir \\ dir.name)
            bd.makeDirectory()
            dir.copyTo(bd)
          }
      }
  }

}
