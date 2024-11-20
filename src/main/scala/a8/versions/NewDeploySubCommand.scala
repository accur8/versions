package a8.versions

import a8.shared.SharedImports.*
import a8.shared.CompanionGen
import a8.shared.SharedImports.*
import a8.versions.NewDeploySubCommand.{Deploy, InstallDescriptor}
import a8.versions.NixGeneratorSubCommand.ConfigFile
import io.accur8.neodeploy.VFileSystem
import io.accur8.neodeploy.model.{DomainName, ListenPort, ServerName, SupervisorDescriptor, VersionBranch}
import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedRepository}
import io.accur8.neodeploy.systemstate.SystemState.JavaAppInstall
import io.accur8.neodeploy.systemstate.SystemStateModel.{Command, Environ, M}
import a8.shared.FileSystem
import io.accur8.neodeploy.Deployable.AppDeployable


/**
 * TODO ??? properly write the version.properties file
 * TODO ??? commit on success
 * TODO ??? app not found message
 */
object NewDeploySubCommand {

  object InstallDescriptor extends MxNewDeploySubCommand.MxInstallDescriptor {
    def fromApp(ra: ResolvedApp, version: ParsedVersion): InstallDescriptor =
      val javaApp = ra.descriptor.install.asInstanceOf[io.accur8.neodeploy.model.Install.JavaApp]
      InstallDescriptor(
        name = ra.name.value,
        organization = javaApp.organization.value,
        artifact = javaApp.artifact.value,
        version = version.toString(),
        installDir = ra.user.home.subdir("apps").subdir(ra.name.value).path,
        webappExplode = javaApp.webappExplode,
        mainClass = javaApp.mainClass,
        branch = javaApp.defaultBranch.map(_.value).getOrElse("master"),
        repo = javaApp.repository.map(_.value).getOrElse(""),
        javaRuntimeVersion = javaApp.javaVersion.value.toString,
        backupDir =  ra.user.home.subdir("apps").subdir("backups").path,
      )
  }

  @CompanionGen
  case class InstallDescriptor(
    name: String,
    organization: String,
    artifact: String,
    version: String,
    installDir: String,
    webappExplode: Boolean,
    mainClass: String,
    branch: String,
    repo: String,
    javaRuntimeVersion: String,
    backupDir: String
  )

}

case class NewDeploySubCommand(rootDir: FileSystem.Directory, resolvedRepo: ResolvedRepository, deploys: Vector[AppDeployable]) extends LoggingF {

  def runM: M[Unit] =
    deploys
      .map(deployApp)
      .sequence
      .unit

  def resolveVersion(appDeploy: AppDeployable): M[ParsedVersion] = {
    val app = appDeploy.resolvedApp
    appDeploy.resolvedApp.
    val version = versionBranch.versionOpt.getOrError("must supply version")
    val javaApp = app.descriptor.install.asInstanceOf[JavaAppInstall]
    version match {
      case "latest" | "current" =>
        val resolvedVersion =
          if (version == "current")
            javaApp.version
          else {
            val parsedVersion = ParsedVersion.parse(javaApp.version).get
            val remoteVersions = resolvedRepo.remoteVersions(javaApp.module)
            val localVersions = resolvedRepo.localVersions(javaApp.module)
            val versions = if (BuildType.useLocalRepo) localVersions ++ remoteVersions else remoteVersions
            versions
              .filter(_.buildInfo.exists(_.branch == BranchName(javaApp.defaultBranch.getOrElse("master"))))
              .sorted(ParsedVersion.orderingByMajorMinorPathBuildTimestamp)
              .lastOption
              .getOrElse(sys.error(s"unable to find version for ${javaApp.module}"))
          }
        M.succeed(resolvedVersion)
      case _ =>
        M.succeed(ParsedVersion.parse(version).get)
    }
  }

  def deployApp(appDeploy: AppDeployable): M[Unit] = zattempt {
    val app = appDeploy.resolvedApp
    val version = appDeploy.versionOpt.getOrError("must supply version")
    val timestamp = a8.shared.FileSystem.fileSystemCompatibleTimestamp() + "-" + System.nanoTime()
    val workDir = rootDir.subdir("work").subdir("work-" + app.name.value + "-" + timestamp)
    val installDescriptor = InstallDescriptor.fromApp(app, version)
    val remotePath = app.user.sshName + ":" + workDir.name

    try {
      FileSystem.dir(app.gitDirectory.path).copyTo(workDir)

      workDir
        .file("install-descriptor.json")
        .write(installDescriptor.prettyJson)

      val rsyncExitCode =
        Exec("rsync", workDir.absolutePath, remotePath)
          .execInline()

      if (rsyncExitCode != 0)
        sys.error(s"rsync failed with exit code $rsyncExitCode")

      val sshExitCode =
        Exec("ssh", app.user.sshName, "--", "a8-install", workDir.name)
          .execInline()

      if (sshExitCode != 0)
        sys.error(s"ssh failed with exit code $sshExitCode")
    } finally {
      workDir.deleteChildren()
      workDir.delete()
    }

  }

}
