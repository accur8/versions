package a8.versions

import a8.shared.FileSystem.File
import a8.common.logging.Logging
import a8.versions.Build.BuildType

import java.util.Properties
import scala.jdk.CollectionConverters._

object UpgradeVersions extends Logging {

  def runUpgrade(versionDotPropsFile: File, repositoryOps: RepositoryOps)(implicit buildType: BuildType): Unit = {

    lazy val versionInfo: Map[String, String] = {
      val props = new Properties()
      versionDotPropsFile.withInputStream(props.load)
      props.asScala.toMap
    }


    lazy val upgrades: Map[String, Upgrade] =
      versionInfo
        .filter(_._1.endsWith(".upgrade"))
        .map(t => t._1 -> Upgrade.parse(t._2))


    lazy val resolvedVersions =
      upgrades.map(t => t._1 -> t._2.resolveVersion(upgrades, repositoryOps))

    lazy val newVersions =
      versionInfo
        .filterNot(_._1.endsWith(".upgrade"))
        .map { t =>
          t._1 -> resolvedVersions.get(t._1 + ".upgrade").getOrElse(t._2)
        }

    val propsFileContents =
      newVersions
        .toIndexedSeq
        .sortBy(_._1)
        .map { t =>
          val upgradeKey = t._1 + ".upgrade"
          val upgradeEntry =
            versionInfo
              .get(upgradeKey)
              .map(upgradeValue => s"${upgradeKey} = ${upgradeValue}")

          (upgradeEntry ++ Some(s"${t._1} = ${t._2}")).mkString("\n")

        }
        .mkString("\n\n", "\n\n", "\n\n")

    logger.debug("====================== " + versionDotPropsFile.canonicalPath + "\n" + propsFileContents)

    versionDotPropsFile.write(propsFileContents)
  }


}
