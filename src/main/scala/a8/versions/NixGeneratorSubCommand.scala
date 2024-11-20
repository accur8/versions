package a8.versions

import io.accur8.neodeploy.resolvedmodel.{ResolvedApp, ResolvedRepository}
import a8.shared.SharedImports.*
import a8.versions.NixGeneratorSubCommand.ConfigFile
import io.accur8.neodeploy.VFileSystem
import io.accur8.neodeploy.model.{DomainName, ListenPort, ServerName, SupervisorDescriptor}
import io.accur8.neodeploy.systemstate.SystemStateModel.{Command, Environ, M}

object NixGeneratorSubCommand {

  case class ConfigFile(
    filename: String,
    content: String,
  )

}

case class NixGeneratorSubCommand(resolvedRepo: ResolvedRepository) extends LoggingF{

  def runM: M[Unit] = {
    val dir = a8.shared.ZFileSystem.dir("/Users/glen/code/accur8/proxmox-hosts/nixgen/")
    val outputConfigs =
      configFiles
        .map { cf =>
          dir
            .file(cf.filename)
            .write(cf.content)
        }
        .sequence
        .logVoid
    for {
      _ <- dir.deleteChildren
      _ <- dir.makeDirectories
      _ <- outputConfigs
    } yield ()
  }

  def configFiles: Vector[ConfigFile] =
    resolvedRepo
      .servers
      .flatMap(server =>
        server
          .applications
          .flatMap(configs)
      )

  def configs(app: ResolvedApp): Vector[ConfigFile] = {
    val caddyConfigOpt = caddyConfig(app)
    val supervisorConfigOpt = supervisorConfig(app)
    (caddyConfigOpt ++ supervisorConfigOpt).toVector
  }

  def caddyConfig(app: ResolvedApp): Option[ConfigFile] = {
    val contentOpt =
      (app.descriptor.caddyConfig, app.descriptor.listenPort) match {
        case (Some(cc), _) =>
          Some(cc)
        case (_, Some(listenPort)) =>
          Some(caddyConfigSnippet(app, listenPort, app.descriptor.resolvedDomainNames))
        case _ =>
          None
      }
    contentOpt
      .map( content =>
        ConfigFile(
          filename = z"caddy/${app.server.name}/${app.name.value}.caddy",
          content = content,
        )
      )
  }

  def supervisorConfig(app: ResolvedApp): Option[ConfigFile] = {
    val sd =
      app.descriptor.launcher match {
        case sd0: SupervisorDescriptor =>
          sd0
        case _ =>
          SupervisorDescriptor.empty
      }
    Some(supervisorConfigContents(app, sd))
  }


  def supervisorConfigContents(app: ResolvedApp, supervisor: SupervisorDescriptor): ConfigFile = {

    //    def pathZStringer[A <: a8.shared.ZFileSystem.Path]: ZStringer[A] =
    //      new ZStringer[A] {
    //        override def toZString(a: A): ZString =
    //          a.asNioPath.toAbsolutePath.toString
    //      }

    //    implicit val dirZStringer = pathZStringer[Directory]
    //    implicit val fileZStringer = pathZStringer[File]

    import app.descriptor._

    val resolvedAutoStart = supervisor.autoStart.getOrElse(true)
    val resolvedStartRetries = supervisor.startRetries.getOrElse(0)
    val resolvedAutoRestart = supervisor.autoRestart.getOrElse(true)
    val resolvedStartSecs = supervisor.startSecs.getOrElse(5)

    val appsRoot = app.user.appsRootDirectory
    val bin = appsRoot.subdir("bin").file(app.descriptor.name.value)
    val logsDir = appsRoot.subdir("logs")
    val appDir = appsRoot.subdir(app.descriptor.name.value)
    val tempDir = appDir.subdir("tmp")
    val command = app.descriptor.install.command(app.descriptor, appDir, appsRoot)
    val workingDir: VFileSystem.Directory = command.workingDirectory.getOrElse(appDir)

    val content = z"""
[program:${app.descriptor.name}]

command = ${command.args.mkString(" ")}

directory = ${workingDir.absPath}

autostart       = ${resolvedAutoStart}
autorestart     = ${resolvedAutoRestart}
startretries    = ${resolvedStartRetries}
startsecs       = ${resolvedStartSecs}
redirect_stderr = true
user            = ${app.user.login}

""".trim

    ConfigFile(
      filename = z"supervisor/${app.server.name}/${app.name.value}.conf",
      content = content,
    )

  }

  def caddyConfigSnippet(app: ResolvedApp, listenPort: ListenPort, domains: Iterable[DomainName]): String =
    z"""
${domains.map(_.value).mkString(", ")} {
  encode gzip
  reverse_proxy ${app.server.vpnName}:${listenPort}
}
      """.trim
}
