package example


import a8.appinstaller.AppInstallerConfig.LibDirKind
import a8.appinstaller.{AppInstaller, AppInstallerConfig, InstallBuilder}
import a8.versions.{ParsedVersion, RepositoryOps}
import a8.versions.predef.*
import io.accur8.neodeploy.model.*
import a8.versions.model.*

object AppInstallerDemo {



  def main(args: Array[String]) = {

    val config =
      AppInstallerConfig(
        organization = Organization("a8"),
        artifact = Artifact("a8-qubes-dist_2.12"),
//        version = "2.7.0-20180410_0910_master",
        version = ParsedVersion.parse("1.2.3").get,
        branch = Some(BranchName("master")),
        installDir = Some("/Users/glen/_a/qubes-install"),
        libDirKind = Some(LibDirKind.Repo),
      )

    val installer = new AppInstaller(config, RepositoryOps.default)
    installer.execute()

  }

}
