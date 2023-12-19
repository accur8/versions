package io.accur8.neodeploy

/**

  WARNING THIS IS GENERATED CODE.  DO NOT EDIT.

  The only manually maintained code is the code between the //==== (normally where you add your imports)

*/

//====
import VFileSystem._
import a8.shared.json.ast.{JsDoc, JsObj, JsVal}
import a8.versions.RepositoryOps.RepoConfigPrefix
import sttp.model.Uri
import io.accur8.neodeploy.model.DockerDescriptor.UninstallAction
import io.accur8.neodeploy.model._
import io.accur8.neodeploy.model.Install.{JavaApp, Manual}
import systemstate.SystemStateModel.Command
import a8.shared.jdbcf.DatabaseConfig.Password
import a8.versions.model.BranchName
//====

import a8.shared.Meta.{CaseClassParm, Generator, Constructors}



object Mxmodel {
  
  trait MxJavaApp {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[JavaApp,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[JavaApp,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[JavaApp,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.organization)
          .addField(_.artifact)
          .addField(_.version)
          .addField(_.defaultBranch)
          .addField(_.webappExplode)
          .addField(_.jvmArgs)
          .addField(_.appArgs)
          .addField(_.mainClass)
          .addField(_.javaVersion)
          .addField(_.repository)
      )
      .build
    
    
    given scala.CanEqual[JavaApp, JavaApp] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[JavaApp,parameters.type] =  {
      val constructors = Constructors[JavaApp](10, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val organization: CaseClassParm[JavaApp,Organization] = CaseClassParm[JavaApp,Organization]("organization", _.organization, (d,v) => d.copy(organization = v), None, 0)
      lazy val artifact: CaseClassParm[JavaApp,Artifact] = CaseClassParm[JavaApp,Artifact]("artifact", _.artifact, (d,v) => d.copy(artifact = v), None, 1)
      lazy val version: CaseClassParm[JavaApp,Version] = CaseClassParm[JavaApp,Version]("version", _.version, (d,v) => d.copy(version = v), None, 2)
      lazy val defaultBranch: CaseClassParm[JavaApp,Option[BranchName]] = CaseClassParm[JavaApp,Option[BranchName]]("defaultBranch", _.defaultBranch, (d,v) => d.copy(defaultBranch = v), Some(()=> None), 3)
      lazy val webappExplode: CaseClassParm[JavaApp,Boolean] = CaseClassParm[JavaApp,Boolean]("webappExplode", _.webappExplode, (d,v) => d.copy(webappExplode = v), Some(()=> true), 4)
      lazy val jvmArgs: CaseClassParm[JavaApp,Iterable[String]] = CaseClassParm[JavaApp,Iterable[String]]("jvmArgs", _.jvmArgs, (d,v) => d.copy(jvmArgs = v), Some(()=> None), 5)
      lazy val appArgs: CaseClassParm[JavaApp,Iterable[String]] = CaseClassParm[JavaApp,Iterable[String]]("appArgs", _.appArgs, (d,v) => d.copy(appArgs = v), Some(()=> Iterable.empty), 6)
      lazy val mainClass: CaseClassParm[JavaApp,String] = CaseClassParm[JavaApp,String]("mainClass", _.mainClass, (d,v) => d.copy(mainClass = v), None, 7)
      lazy val javaVersion: CaseClassParm[JavaApp,JavaVersion] = CaseClassParm[JavaApp,JavaVersion]("javaVersion", _.javaVersion, (d,v) => d.copy(javaVersion = v), Some(()=> JavaVersion(11)), 8)
      lazy val repository: CaseClassParm[JavaApp,Option[RepoConfigPrefix]] = CaseClassParm[JavaApp,Option[RepoConfigPrefix]]("repository", _.repository, (d,v) => d.copy(repository = v), Some(()=> None), 9)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): JavaApp = {
        JavaApp(
          organization = values(0).asInstanceOf[Organization],
          artifact = values(1).asInstanceOf[Artifact],
          version = values(2).asInstanceOf[Version],
          defaultBranch = values(3).asInstanceOf[Option[BranchName]],
          webappExplode = values(4).asInstanceOf[Boolean],
          jvmArgs = values(5).asInstanceOf[Iterable[String]],
          appArgs = values(6).asInstanceOf[Iterable[String]],
          mainClass = values(7).asInstanceOf[String],
          javaVersion = values(8).asInstanceOf[JavaVersion],
          repository = values(9).asInstanceOf[Option[RepoConfigPrefix]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): JavaApp = {
        val value =
          JavaApp(
            organization = values.next().asInstanceOf[Organization],
            artifact = values.next().asInstanceOf[Artifact],
            version = values.next().asInstanceOf[Version],
            defaultBranch = values.next().asInstanceOf[Option[BranchName]],
            webappExplode = values.next().asInstanceOf[Boolean],
            jvmArgs = values.next().asInstanceOf[Iterable[String]],
            appArgs = values.next().asInstanceOf[Iterable[String]],
            mainClass = values.next().asInstanceOf[String],
            javaVersion = values.next().asInstanceOf[JavaVersion],
            repository = values.next().asInstanceOf[Option[RepoConfigPrefix]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(organization: Organization, artifact: Artifact, version: Version, defaultBranch: Option[BranchName], webappExplode: Boolean, jvmArgs: Iterable[String], appArgs: Iterable[String], mainClass: String, javaVersion: JavaVersion, repository: Option[RepoConfigPrefix]): JavaApp =
        JavaApp(organization, artifact, version, defaultBranch, webappExplode, jvmArgs, appArgs, mainClass, javaVersion, repository)
    
    }
    
    
    lazy val typeName = "JavaApp"
  
  }
  
  
  
  
  trait MxManual {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[Manual,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[Manual,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[Manual,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.description)
          .addField(_.command)
      )
      .build
    
    
    given scala.CanEqual[Manual, Manual] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[Manual,parameters.type] =  {
      val constructors = Constructors[Manual](2, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val description: CaseClassParm[Manual,String] = CaseClassParm[Manual,String]("description", _.description, (d,v) => d.copy(description = v), Some(()=> "manual install"), 0)
      lazy val command: CaseClassParm[Manual,Command] = CaseClassParm[Manual,Command]("command", _.command, (d,v) => d.copy(command = v), None, 1)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): Manual = {
        Manual(
          description = values(0).asInstanceOf[String],
          command = values(1).asInstanceOf[Command],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): Manual = {
        val value =
          Manual(
            description = values.next().asInstanceOf[String],
            command = values.next().asInstanceOf[Command],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(description: String, command: Command): Manual =
        Manual(description, command)
    
    }
    
    
    lazy val typeName = "Manual"
  
  }
  
  
  
  
  trait MxSupervisorDescriptor {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[SupervisorDescriptor,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[SupervisorDescriptor,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[SupervisorDescriptor,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.autoStart)
          .addField(_.autoRestart)
          .addField(_.startRetries)
          .addField(_.startSecs)
      )
      .build
    
    
    given scala.CanEqual[SupervisorDescriptor, SupervisorDescriptor] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[SupervisorDescriptor,parameters.type] =  {
      val constructors = Constructors[SupervisorDescriptor](4, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val autoStart: CaseClassParm[SupervisorDescriptor,Option[Boolean]] = CaseClassParm[SupervisorDescriptor,Option[Boolean]]("autoStart", _.autoStart, (d,v) => d.copy(autoStart = v), Some(()=> None), 0)
      lazy val autoRestart: CaseClassParm[SupervisorDescriptor,Option[Boolean]] = CaseClassParm[SupervisorDescriptor,Option[Boolean]]("autoRestart", _.autoRestart, (d,v) => d.copy(autoRestart = v), Some(()=> None), 1)
      lazy val startRetries: CaseClassParm[SupervisorDescriptor,Option[Int]] = CaseClassParm[SupervisorDescriptor,Option[Int]]("startRetries", _.startRetries, (d,v) => d.copy(startRetries = v), Some(()=> None), 2)
      lazy val startSecs: CaseClassParm[SupervisorDescriptor,Option[Int]] = CaseClassParm[SupervisorDescriptor,Option[Int]]("startSecs", _.startSecs, (d,v) => d.copy(startSecs = v), Some(()=> None), 3)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): SupervisorDescriptor = {
        SupervisorDescriptor(
          autoStart = values(0).asInstanceOf[Option[Boolean]],
          autoRestart = values(1).asInstanceOf[Option[Boolean]],
          startRetries = values(2).asInstanceOf[Option[Int]],
          startSecs = values(3).asInstanceOf[Option[Int]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): SupervisorDescriptor = {
        val value =
          SupervisorDescriptor(
            autoStart = values.next().asInstanceOf[Option[Boolean]],
            autoRestart = values.next().asInstanceOf[Option[Boolean]],
            startRetries = values.next().asInstanceOf[Option[Int]],
            startSecs = values.next().asInstanceOf[Option[Int]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(autoStart: Option[Boolean], autoRestart: Option[Boolean], startRetries: Option[Int], startSecs: Option[Int]): SupervisorDescriptor =
        SupervisorDescriptor(autoStart, autoRestart, startRetries, startSecs)
    
    }
    
    
    lazy val typeName = "SupervisorDescriptor"
  
  }
  
  
  
  
  trait MxSystemdDescriptor {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[SystemdDescriptor,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[SystemdDescriptor,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[SystemdDescriptor,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.unitName)
          .addField(_.environment)
          .addField(_.onCalendar)
          .addField(_.persistent)
          .addField(_.`type`)
          .addField(_.enableService)
      )
      .build
    
    
    given scala.CanEqual[SystemdDescriptor, SystemdDescriptor] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[SystemdDescriptor,parameters.type] =  {
      val constructors = Constructors[SystemdDescriptor](6, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val unitName: CaseClassParm[SystemdDescriptor,Option[String]] = CaseClassParm[SystemdDescriptor,Option[String]]("unitName", _.unitName, (d,v) => d.copy(unitName = v), Some(()=> None), 0)
      lazy val environment: CaseClassParm[SystemdDescriptor,Vector[String]] = CaseClassParm[SystemdDescriptor,Vector[String]]("environment", _.environment, (d,v) => d.copy(environment = v), Some(()=> Vector.empty), 1)
      lazy val onCalendar: CaseClassParm[SystemdDescriptor,Option[OnCalendarValue]] = CaseClassParm[SystemdDescriptor,Option[OnCalendarValue]]("onCalendar", _.onCalendar, (d,v) => d.copy(onCalendar = v), Some(()=> None), 2)
      lazy val persistent: CaseClassParm[SystemdDescriptor,Option[Boolean]] = CaseClassParm[SystemdDescriptor,Option[Boolean]]("persistent", _.persistent, (d,v) => d.copy(persistent = v), Some(()=> None), 3)
      lazy val `type`: CaseClassParm[SystemdDescriptor,String] = CaseClassParm[SystemdDescriptor,String]("type", _.`type`, (d,v) => d.copy(`type` = v), Some(()=> "simple"), 4)
      lazy val enableService: CaseClassParm[SystemdDescriptor,Boolean] = CaseClassParm[SystemdDescriptor,Boolean]("enableService", _.enableService, (d,v) => d.copy(enableService = v), Some(()=> true), 5)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): SystemdDescriptor = {
        SystemdDescriptor(
          unitName = values(0).asInstanceOf[Option[String]],
          environment = values(1).asInstanceOf[Vector[String]],
          onCalendar = values(2).asInstanceOf[Option[OnCalendarValue]],
          persistent = values(3).asInstanceOf[Option[Boolean]],
          `type` = values(4).asInstanceOf[String],
          enableService = values(5).asInstanceOf[Boolean],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): SystemdDescriptor = {
        val value =
          SystemdDescriptor(
            unitName = values.next().asInstanceOf[Option[String]],
            environment = values.next().asInstanceOf[Vector[String]],
            onCalendar = values.next().asInstanceOf[Option[OnCalendarValue]],
            persistent = values.next().asInstanceOf[Option[Boolean]],
            `type` = values.next().asInstanceOf[String],
            enableService = values.next().asInstanceOf[Boolean],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(unitName: Option[String], environment: Vector[String], onCalendar: Option[OnCalendarValue], persistent: Option[Boolean], `type`: String, enableService: Boolean): SystemdDescriptor =
        SystemdDescriptor(unitName, environment, onCalendar, persistent, `type`, enableService)
    
    }
    
    
    lazy val typeName = "SystemdDescriptor"
  
  }
  
  
  
  
  trait MxDockerDescriptor {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[DockerDescriptor,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[DockerDescriptor,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[DockerDescriptor,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.name)
          .addField(_.args)
          .addField(_.uninstallAction)
      )
      .build
    
    
    given scala.CanEqual[DockerDescriptor, DockerDescriptor] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[DockerDescriptor,parameters.type] =  {
      val constructors = Constructors[DockerDescriptor](3, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val name: CaseClassParm[DockerDescriptor,String] = CaseClassParm[DockerDescriptor,String]("name", _.name, (d,v) => d.copy(name = v), None, 0)
      lazy val args: CaseClassParm[DockerDescriptor,Vector[String]] = CaseClassParm[DockerDescriptor,Vector[String]]("args", _.args, (d,v) => d.copy(args = v), None, 1)
      lazy val uninstallAction: CaseClassParm[DockerDescriptor,UninstallAction] = CaseClassParm[DockerDescriptor,UninstallAction]("uninstallAction", _.uninstallAction, (d,v) => d.copy(uninstallAction = v), Some(()=> UninstallAction.Stop), 2)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): DockerDescriptor = {
        DockerDescriptor(
          name = values(0).asInstanceOf[String],
          args = values(1).asInstanceOf[Vector[String]],
          uninstallAction = values(2).asInstanceOf[UninstallAction],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): DockerDescriptor = {
        val value =
          DockerDescriptor(
            name = values.next().asInstanceOf[String],
            args = values.next().asInstanceOf[Vector[String]],
            uninstallAction = values.next().asInstanceOf[UninstallAction],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(name: String, args: Vector[String], uninstallAction: UninstallAction): DockerDescriptor =
        DockerDescriptor(name, args, uninstallAction)
    
    }
    
    
    lazy val typeName = "DockerDescriptor"
  
  }
  
  
  
  
  trait MxApplicationDescriptor {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[ApplicationDescriptor,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[ApplicationDescriptor,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[ApplicationDescriptor,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.name)
          .addField(_.install)
          .addField(_.caddyConfig)
          .addField(_.listenPort)
          .addField(_.stopServerCommand)
          .addField(_.startServerCommand)
          .addField(_.domainName)
          .addField(_.domainNames)
          .addField(_.setup)
          .addField(_.launcher)
      )
      .build
    
    
    given scala.CanEqual[ApplicationDescriptor, ApplicationDescriptor] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[ApplicationDescriptor,parameters.type] =  {
      val constructors = Constructors[ApplicationDescriptor](10, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val name: CaseClassParm[ApplicationDescriptor,ApplicationName] = CaseClassParm[ApplicationDescriptor,ApplicationName]("name", _.name, (d,v) => d.copy(name = v), None, 0)
      lazy val install: CaseClassParm[ApplicationDescriptor,Install] = CaseClassParm[ApplicationDescriptor,Install]("install", _.install, (d,v) => d.copy(install = v), None, 1)
      lazy val caddyConfig: CaseClassParm[ApplicationDescriptor,Option[String]] = CaseClassParm[ApplicationDescriptor,Option[String]]("caddyConfig", _.caddyConfig, (d,v) => d.copy(caddyConfig = v), Some(()=> None), 2)
      lazy val listenPort: CaseClassParm[ApplicationDescriptor,Option[ListenPort]] = CaseClassParm[ApplicationDescriptor,Option[ListenPort]]("listenPort", _.listenPort, (d,v) => d.copy(listenPort = v), Some(()=> None), 3)
      lazy val stopServerCommand: CaseClassParm[ApplicationDescriptor,Option[Command]] = CaseClassParm[ApplicationDescriptor,Option[Command]]("stopServerCommand", _.stopServerCommand, (d,v) => d.copy(stopServerCommand = v), Some(()=> None), 4)
      lazy val startServerCommand: CaseClassParm[ApplicationDescriptor,Option[Command]] = CaseClassParm[ApplicationDescriptor,Option[Command]]("startServerCommand", _.startServerCommand, (d,v) => d.copy(startServerCommand = v), Some(()=> None), 5)
      lazy val domainName: CaseClassParm[ApplicationDescriptor,Option[DomainName]] = CaseClassParm[ApplicationDescriptor,Option[DomainName]]("domainName", _.domainName, (d,v) => d.copy(domainName = v), Some(()=> None), 6)
      lazy val domainNames: CaseClassParm[ApplicationDescriptor,Vector[DomainName]] = CaseClassParm[ApplicationDescriptor,Vector[DomainName]]("domainNames", _.domainNames, (d,v) => d.copy(domainNames = v), Some(()=> Vector.empty), 7)
      lazy val setup: CaseClassParm[ApplicationDescriptor,ApplicationSetupDescriptor] = CaseClassParm[ApplicationDescriptor,ApplicationSetupDescriptor]("setup", _.setup, (d,v) => d.copy(setup = v), Some(()=> ApplicationSetupDescriptor.empty), 8)
      lazy val launcher: CaseClassParm[ApplicationDescriptor,LauncherDescriptor] = CaseClassParm[ApplicationDescriptor,LauncherDescriptor]("launcher", _.launcher, (d,v) => d.copy(launcher = v), Some(()=> SupervisorDescriptor.empty), 9)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): ApplicationDescriptor = {
        ApplicationDescriptor(
          name = values(0).asInstanceOf[ApplicationName],
          install = values(1).asInstanceOf[Install],
          caddyConfig = values(2).asInstanceOf[Option[String]],
          listenPort = values(3).asInstanceOf[Option[ListenPort]],
          stopServerCommand = values(4).asInstanceOf[Option[Command]],
          startServerCommand = values(5).asInstanceOf[Option[Command]],
          domainName = values(6).asInstanceOf[Option[DomainName]],
          domainNames = values(7).asInstanceOf[Vector[DomainName]],
          setup = values(8).asInstanceOf[ApplicationSetupDescriptor],
          launcher = values(9).asInstanceOf[LauncherDescriptor],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): ApplicationDescriptor = {
        val value =
          ApplicationDescriptor(
            name = values.next().asInstanceOf[ApplicationName],
            install = values.next().asInstanceOf[Install],
            caddyConfig = values.next().asInstanceOf[Option[String]],
            listenPort = values.next().asInstanceOf[Option[ListenPort]],
            stopServerCommand = values.next().asInstanceOf[Option[Command]],
            startServerCommand = values.next().asInstanceOf[Option[Command]],
            domainName = values.next().asInstanceOf[Option[DomainName]],
            domainNames = values.next().asInstanceOf[Vector[DomainName]],
            setup = values.next().asInstanceOf[ApplicationSetupDescriptor],
            launcher = values.next().asInstanceOf[LauncherDescriptor],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(name: ApplicationName, install: Install, caddyConfig: Option[String], listenPort: Option[ListenPort], stopServerCommand: Option[Command], startServerCommand: Option[Command], domainName: Option[DomainName], domainNames: Vector[DomainName], setup: ApplicationSetupDescriptor, launcher: LauncherDescriptor): ApplicationDescriptor =
        ApplicationDescriptor(name, install, caddyConfig, listenPort, stopServerCommand, startServerCommand, domainName, domainNames, setup, launcher)
    
    }
    
    
    lazy val typeName = "ApplicationDescriptor"
  
  }
  
  
  
  
  trait MxApplicationSetupDescriptor {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[ApplicationSetupDescriptor,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[ApplicationSetupDescriptor,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[ApplicationSetupDescriptor,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.database)
          .addField(_.qubes)
      )
      .build
    
    
    given scala.CanEqual[ApplicationSetupDescriptor, ApplicationSetupDescriptor] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[ApplicationSetupDescriptor,parameters.type] =  {
      val constructors = Constructors[ApplicationSetupDescriptor](2, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val database: CaseClassParm[ApplicationSetupDescriptor,Option[DatabaseSetupDescriptor]] = CaseClassParm[ApplicationSetupDescriptor,Option[DatabaseSetupDescriptor]]("database", _.database, (d,v) => d.copy(database = v), Some(()=> None), 0)
      lazy val qubes: CaseClassParm[ApplicationSetupDescriptor,Option[DomainName]] = CaseClassParm[ApplicationSetupDescriptor,Option[DomainName]]("qubes", _.qubes, (d,v) => d.copy(qubes = v), Some(()=> None), 1)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): ApplicationSetupDescriptor = {
        ApplicationSetupDescriptor(
          database = values(0).asInstanceOf[Option[DatabaseSetupDescriptor]],
          qubes = values(1).asInstanceOf[Option[DomainName]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): ApplicationSetupDescriptor = {
        val value =
          ApplicationSetupDescriptor(
            database = values.next().asInstanceOf[Option[DatabaseSetupDescriptor]],
            qubes = values.next().asInstanceOf[Option[DomainName]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(database: Option[DatabaseSetupDescriptor], qubes: Option[DomainName]): ApplicationSetupDescriptor =
        ApplicationSetupDescriptor(database, qubes)
    
    }
    
    
    lazy val typeName = "ApplicationSetupDescriptor"
  
  }
  
  
  
  
  trait MxUserPassword {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[UserPassword,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[UserPassword,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[UserPassword,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.user)
          .addField(_.rawPassword)
      )
      .build
    
    
    given scala.CanEqual[UserPassword, UserPassword] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[UserPassword,parameters.type] =  {
      val constructors = Constructors[UserPassword](2, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val user: CaseClassParm[UserPassword,UserLogin] = CaseClassParm[UserPassword,UserLogin]("user", _.user, (d,v) => d.copy(user = v), None, 0)
      lazy val rawPassword: CaseClassParm[UserPassword,String] = CaseClassParm[UserPassword,String]("rawPassword", _.rawPassword, (d,v) => d.copy(rawPassword = v), None, 1)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): UserPassword = {
        UserPassword(
          user = values(0).asInstanceOf[UserLogin],
          rawPassword = values(1).asInstanceOf[String],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): UserPassword = {
        val value =
          UserPassword(
            user = values.next().asInstanceOf[UserLogin],
            rawPassword = values.next().asInstanceOf[String],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(user: UserLogin, rawPassword: String): UserPassword =
        UserPassword(user, rawPassword)
    
    }
    
    
    lazy val typeName = "UserPassword"
  
  }
  
  
  
  
  trait MxPasswords {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[Passwords,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[Passwords,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[Passwords,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.userPasswords)
      )
      .build
    
    
    given scala.CanEqual[Passwords, Passwords] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[Passwords,parameters.type] =  {
      val constructors = Constructors[Passwords](1, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val userPasswords: CaseClassParm[Passwords,Vector[UserPassword]] = CaseClassParm[Passwords,Vector[UserPassword]]("userPasswords", _.userPasswords, (d,v) => d.copy(userPasswords = v), None, 0)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): Passwords = {
        Passwords(
          userPasswords = values(0).asInstanceOf[Vector[UserPassword]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): Passwords = {
        val value =
          Passwords(
            userPasswords = values.next().asInstanceOf[Vector[UserPassword]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(userPasswords: Vector[UserPassword]): Passwords =
        Passwords(userPasswords)
    
    }
    
    
    lazy val typeName = "Passwords"
  
  }
  
  
  
  
  trait MxDatabaseSetupDescriptor {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[DatabaseSetupDescriptor,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[DatabaseSetupDescriptor,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[DatabaseSetupDescriptor,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.databaseServer)
          .addField(_.databaseName)
          .addField(_.owner)
          .addField(_.extraUsers)
      )
      .build
    
    
    given scala.CanEqual[DatabaseSetupDescriptor, DatabaseSetupDescriptor] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[DatabaseSetupDescriptor,parameters.type] =  {
      val constructors = Constructors[DatabaseSetupDescriptor](4, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val databaseServer: CaseClassParm[DatabaseSetupDescriptor,DomainName] = CaseClassParm[DatabaseSetupDescriptor,DomainName]("databaseServer", _.databaseServer, (d,v) => d.copy(databaseServer = v), None, 0)
      lazy val databaseName: CaseClassParm[DatabaseSetupDescriptor,DatabaseName] = CaseClassParm[DatabaseSetupDescriptor,DatabaseName]("databaseName", _.databaseName, (d,v) => d.copy(databaseName = v), None, 1)
      lazy val owner: CaseClassParm[DatabaseSetupDescriptor,UserLogin] = CaseClassParm[DatabaseSetupDescriptor,UserLogin]("owner", _.owner, (d,v) => d.copy(owner = v), None, 2)
      lazy val extraUsers: CaseClassParm[DatabaseSetupDescriptor,Iterable[DatabaseUserDescriptor]] = CaseClassParm[DatabaseSetupDescriptor,Iterable[DatabaseUserDescriptor]]("extraUsers", _.extraUsers, (d,v) => d.copy(extraUsers = v), Some(()=> Iterable.empty), 3)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): DatabaseSetupDescriptor = {
        DatabaseSetupDescriptor(
          databaseServer = values(0).asInstanceOf[DomainName],
          databaseName = values(1).asInstanceOf[DatabaseName],
          owner = values(2).asInstanceOf[UserLogin],
          extraUsers = values(3).asInstanceOf[Iterable[DatabaseUserDescriptor]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): DatabaseSetupDescriptor = {
        val value =
          DatabaseSetupDescriptor(
            databaseServer = values.next().asInstanceOf[DomainName],
            databaseName = values.next().asInstanceOf[DatabaseName],
            owner = values.next().asInstanceOf[UserLogin],
            extraUsers = values.next().asInstanceOf[Iterable[DatabaseUserDescriptor]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(databaseServer: DomainName, databaseName: DatabaseName, owner: UserLogin, extraUsers: Iterable[DatabaseUserDescriptor]): DatabaseSetupDescriptor =
        DatabaseSetupDescriptor(databaseServer, databaseName, owner, extraUsers)
    
    }
    
    
    lazy val typeName = "DatabaseSetupDescriptor"
  
  }
  
  
  
  
  trait MxDatabaseUserDescriptor {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[DatabaseUserDescriptor,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[DatabaseUserDescriptor,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[DatabaseUserDescriptor,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.name)
          .addField(_.roles)
      )
      .build
    
    
    given scala.CanEqual[DatabaseUserDescriptor, DatabaseUserDescriptor] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[DatabaseUserDescriptor,parameters.type] =  {
      val constructors = Constructors[DatabaseUserDescriptor](2, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val name: CaseClassParm[DatabaseUserDescriptor,UserLogin] = CaseClassParm[DatabaseUserDescriptor,UserLogin]("name", _.name, (d,v) => d.copy(name = v), None, 0)
      lazy val roles: CaseClassParm[DatabaseUserDescriptor,Iterable[DatabaseUserRole]] = CaseClassParm[DatabaseUserDescriptor,Iterable[DatabaseUserRole]]("roles", _.roles, (d,v) => d.copy(roles = v), None, 1)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): DatabaseUserDescriptor = {
        DatabaseUserDescriptor(
          name = values(0).asInstanceOf[UserLogin],
          roles = values(1).asInstanceOf[Iterable[DatabaseUserRole]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): DatabaseUserDescriptor = {
        val value =
          DatabaseUserDescriptor(
            name = values.next().asInstanceOf[UserLogin],
            roles = values.next().asInstanceOf[Iterable[DatabaseUserRole]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(name: UserLogin, roles: Iterable[DatabaseUserRole]): DatabaseUserDescriptor =
        DatabaseUserDescriptor(name, roles)
    
    }
    
    
    lazy val typeName = "DatabaseUserDescriptor"
  
  }
  
  
  
  
  trait MxZooFile {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[ZooFile,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[ZooFile,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[ZooFile,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.filename)
          .addField(_.organization)
          .addField(_.artifact)
          .addField(_.zooVersion)
      )
      .build
    
    
    given scala.CanEqual[ZooFile, ZooFile] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[ZooFile,parameters.type] =  {
      val constructors = Constructors[ZooFile](4, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val filename: CaseClassParm[ZooFile,String] = CaseClassParm[ZooFile,String]("filename", _.filename, (d,v) => d.copy(filename = v), None, 0)
      lazy val organization: CaseClassParm[ZooFile,Organization] = CaseClassParm[ZooFile,Organization]("organization", _.organization, (d,v) => d.copy(organization = v), None, 1)
      lazy val artifact: CaseClassParm[ZooFile,Artifact] = CaseClassParm[ZooFile,Artifact]("artifact", _.artifact, (d,v) => d.copy(artifact = v), None, 2)
      lazy val zooVersion: CaseClassParm[ZooFile,Option[String]] = CaseClassParm[ZooFile,Option[String]]("zooVersion", _.zooVersion, (d,v) => d.copy(zooVersion = v), Some(()=> None), 3)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): ZooFile = {
        ZooFile(
          filename = values(0).asInstanceOf[String],
          organization = values(1).asInstanceOf[Organization],
          artifact = values(2).asInstanceOf[Artifact],
          zooVersion = values(3).asInstanceOf[Option[String]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): ZooFile = {
        val value =
          ZooFile(
            filename = values.next().asInstanceOf[String],
            organization = values.next().asInstanceOf[Organization],
            artifact = values.next().asInstanceOf[Artifact],
            zooVersion = values.next().asInstanceOf[Option[String]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(filename: String, organization: Organization, artifact: Artifact, zooVersion: Option[String]): ZooFile =
        ZooFile(filename, organization, artifact, zooVersion)
    
    }
    
    
    lazy val typeName = "ZooFile"
  
  }
  
  
  
  
  trait MxUserDescriptor {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[UserDescriptor,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[UserDescriptor,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[UserDescriptor,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.login)
          .addField(_.aliases)
          .addField(_.authorizedKeys)
          .addField(_.a8VersionsExec)
          .addField(_.home)
          .addField(_.manageSshKeys)
          .addField(_.appInstallDirectory)
          .addField(_.plugins)
      )
      .build
    
    
    given scala.CanEqual[UserDescriptor, UserDescriptor] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[UserDescriptor,parameters.type] =  {
      val constructors = Constructors[UserDescriptor](8, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val login: CaseClassParm[UserDescriptor,UserLogin] = CaseClassParm[UserDescriptor,UserLogin]("login", _.login, (d,v) => d.copy(login = v), None, 0)
      lazy val aliases: CaseClassParm[UserDescriptor,Vector[QualifiedUserName]] = CaseClassParm[UserDescriptor,Vector[QualifiedUserName]]("aliases", _.aliases, (d,v) => d.copy(aliases = v), Some(()=> Vector.empty), 1)
      lazy val authorizedKeys: CaseClassParm[UserDescriptor,Vector[QualifiedUserName]] = CaseClassParm[UserDescriptor,Vector[QualifiedUserName]]("authorizedKeys", _.authorizedKeys, (d,v) => d.copy(authorizedKeys = v), Some(()=> Vector.empty), 2)
      lazy val a8VersionsExec: CaseClassParm[UserDescriptor,Option[String]] = CaseClassParm[UserDescriptor,Option[String]]("a8VersionsExec", _.a8VersionsExec, (d,v) => d.copy(a8VersionsExec = v), Some(()=> None), 3)
      lazy val home: CaseClassParm[UserDescriptor,Option[Directory]] = CaseClassParm[UserDescriptor,Option[Directory]]("home", _.home, (d,v) => d.copy(home = v), Some(()=> None), 4)
      lazy val manageSshKeys: CaseClassParm[UserDescriptor,Boolean] = CaseClassParm[UserDescriptor,Boolean]("manageSshKeys", _.manageSshKeys, (d,v) => d.copy(manageSshKeys = v), Some(()=> true), 5)
      lazy val appInstallDirectory: CaseClassParm[UserDescriptor,Option[AppsRootDirectory]] = CaseClassParm[UserDescriptor,Option[AppsRootDirectory]]("appInstallDirectory", _.appInstallDirectory, (d,v) => d.copy(appInstallDirectory = v), Some(()=> None), 6)
      lazy val plugins: CaseClassParm[UserDescriptor,JsDoc] = CaseClassParm[UserDescriptor,JsDoc]("plugins", _.plugins, (d,v) => d.copy(plugins = v), Some(()=> JsDoc.empty), 7)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): UserDescriptor = {
        UserDescriptor(
          login = values(0).asInstanceOf[UserLogin],
          aliases = values(1).asInstanceOf[Vector[QualifiedUserName]],
          authorizedKeys = values(2).asInstanceOf[Vector[QualifiedUserName]],
          a8VersionsExec = values(3).asInstanceOf[Option[String]],
          home = values(4).asInstanceOf[Option[Directory]],
          manageSshKeys = values(5).asInstanceOf[Boolean],
          appInstallDirectory = values(6).asInstanceOf[Option[AppsRootDirectory]],
          plugins = values(7).asInstanceOf[JsDoc],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): UserDescriptor = {
        val value =
          UserDescriptor(
            login = values.next().asInstanceOf[UserLogin],
            aliases = values.next().asInstanceOf[Vector[QualifiedUserName]],
            authorizedKeys = values.next().asInstanceOf[Vector[QualifiedUserName]],
            a8VersionsExec = values.next().asInstanceOf[Option[String]],
            home = values.next().asInstanceOf[Option[Directory]],
            manageSshKeys = values.next().asInstanceOf[Boolean],
            appInstallDirectory = values.next().asInstanceOf[Option[AppsRootDirectory]],
            plugins = values.next().asInstanceOf[JsDoc],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(login: UserLogin, aliases: Vector[QualifiedUserName], authorizedKeys: Vector[QualifiedUserName], a8VersionsExec: Option[String], home: Option[Directory], manageSshKeys: Boolean, appInstallDirectory: Option[AppsRootDirectory], plugins: JsDoc): UserDescriptor =
        UserDescriptor(login, aliases, authorizedKeys, a8VersionsExec, home, manageSshKeys, appInstallDirectory, plugins)
    
    }
    
    
    lazy val typeName = "UserDescriptor"
  
  }
  
  
  
  
  trait MxRSnapshotClientDescriptor {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[RSnapshotClientDescriptor,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[RSnapshotClientDescriptor,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[RSnapshotClientDescriptor,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.name)
          .addField(_.directories)
          .addField(_.runAt)
          .addField(_.hourly)
          .addField(_.includeExcludeLines)
      )
      .build
    
    
    given scala.CanEqual[RSnapshotClientDescriptor, RSnapshotClientDescriptor] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[RSnapshotClientDescriptor,parameters.type] =  {
      val constructors = Constructors[RSnapshotClientDescriptor](5, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val name: CaseClassParm[RSnapshotClientDescriptor,String] = CaseClassParm[RSnapshotClientDescriptor,String]("name", _.name, (d,v) => d.copy(name = v), None, 0)
      lazy val directories: CaseClassParm[RSnapshotClientDescriptor,Vector[String]] = CaseClassParm[RSnapshotClientDescriptor,Vector[String]]("directories", _.directories, (d,v) => d.copy(directories = v), None, 1)
      lazy val runAt: CaseClassParm[RSnapshotClientDescriptor,OnCalendarValue] = CaseClassParm[RSnapshotClientDescriptor,OnCalendarValue]("runAt", _.runAt, (d,v) => d.copy(runAt = v), None, 2)
      lazy val hourly: CaseClassParm[RSnapshotClientDescriptor,Boolean] = CaseClassParm[RSnapshotClientDescriptor,Boolean]("hourly", _.hourly, (d,v) => d.copy(hourly = v), Some(()=> false), 3)
      lazy val includeExcludeLines: CaseClassParm[RSnapshotClientDescriptor,Iterable[String]] = CaseClassParm[RSnapshotClientDescriptor,Iterable[String]]("includeExcludeLines", _.includeExcludeLines, (d,v) => d.copy(includeExcludeLines = v), Some(()=> Iterable.empty), 4)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): RSnapshotClientDescriptor = {
        RSnapshotClientDescriptor(
          name = values(0).asInstanceOf[String],
          directories = values(1).asInstanceOf[Vector[String]],
          runAt = values(2).asInstanceOf[OnCalendarValue],
          hourly = values(3).asInstanceOf[Boolean],
          includeExcludeLines = values(4).asInstanceOf[Iterable[String]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): RSnapshotClientDescriptor = {
        val value =
          RSnapshotClientDescriptor(
            name = values.next().asInstanceOf[String],
            directories = values.next().asInstanceOf[Vector[String]],
            runAt = values.next().asInstanceOf[OnCalendarValue],
            hourly = values.next().asInstanceOf[Boolean],
            includeExcludeLines = values.next().asInstanceOf[Iterable[String]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(name: String, directories: Vector[String], runAt: OnCalendarValue, hourly: Boolean, includeExcludeLines: Iterable[String]): RSnapshotClientDescriptor =
        RSnapshotClientDescriptor(name, directories, runAt, hourly, includeExcludeLines)
    
    }
    
    
    lazy val typeName = "RSnapshotClientDescriptor"
  
  }
  
  
  
  
  trait MxRSnapshotServerDescriptor {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[RSnapshotServerDescriptor,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[RSnapshotServerDescriptor,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[RSnapshotServerDescriptor,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.name)
          .addField(_.snapshotRootDir)
          .addField(_.configDir)
          .addField(_.logDir)
          .addField(_.runDir)
      )
      .build
    
    
    given scala.CanEqual[RSnapshotServerDescriptor, RSnapshotServerDescriptor] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[RSnapshotServerDescriptor,parameters.type] =  {
      val constructors = Constructors[RSnapshotServerDescriptor](5, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val name: CaseClassParm[RSnapshotServerDescriptor,String] = CaseClassParm[RSnapshotServerDescriptor,String]("name", _.name, (d,v) => d.copy(name = v), None, 0)
      lazy val snapshotRootDir: CaseClassParm[RSnapshotServerDescriptor,RSnapshotRootDirectory] = CaseClassParm[RSnapshotServerDescriptor,RSnapshotRootDirectory]("snapshotRootDir", _.snapshotRootDir, (d,v) => d.copy(snapshotRootDir = v), None, 1)
      lazy val configDir: CaseClassParm[RSnapshotServerDescriptor,RSnapshotConfigDirectory] = CaseClassParm[RSnapshotServerDescriptor,RSnapshotConfigDirectory]("configDir", _.configDir, (d,v) => d.copy(configDir = v), None, 2)
      lazy val logDir: CaseClassParm[RSnapshotServerDescriptor,String] = CaseClassParm[RSnapshotServerDescriptor,String]("logDir", _.logDir, (d,v) => d.copy(logDir = v), Some(()=> "/var/log"), 3)
      lazy val runDir: CaseClassParm[RSnapshotServerDescriptor,String] = CaseClassParm[RSnapshotServerDescriptor,String]("runDir", _.runDir, (d,v) => d.copy(runDir = v), Some(()=> "/var/run"), 4)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): RSnapshotServerDescriptor = {
        RSnapshotServerDescriptor(
          name = values(0).asInstanceOf[String],
          snapshotRootDir = values(1).asInstanceOf[RSnapshotRootDirectory],
          configDir = values(2).asInstanceOf[RSnapshotConfigDirectory],
          logDir = values(3).asInstanceOf[String],
          runDir = values(4).asInstanceOf[String],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): RSnapshotServerDescriptor = {
        val value =
          RSnapshotServerDescriptor(
            name = values.next().asInstanceOf[String],
            snapshotRootDir = values.next().asInstanceOf[RSnapshotRootDirectory],
            configDir = values.next().asInstanceOf[RSnapshotConfigDirectory],
            logDir = values.next().asInstanceOf[String],
            runDir = values.next().asInstanceOf[String],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(name: String, snapshotRootDir: RSnapshotRootDirectory, configDir: RSnapshotConfigDirectory, logDir: String, runDir: String): RSnapshotServerDescriptor =
        RSnapshotServerDescriptor(name, snapshotRootDir, configDir, logDir, runDir)
    
    }
    
    
    lazy val typeName = "RSnapshotServerDescriptor"
  
  }
  
  
  
  
  trait MxPgbackrestClientDescriptor {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[PgbackrestClientDescriptor,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[PgbackrestClientDescriptor,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[PgbackrestClientDescriptor,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.name)
          .addField(_.pgdata)
          .addField(_.stanzaNameOverride)
          .addField(_.onCalendar)
          .addField(_.configFile)
      )
      .build
    
    
    given scala.CanEqual[PgbackrestClientDescriptor, PgbackrestClientDescriptor] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[PgbackrestClientDescriptor,parameters.type] =  {
      val constructors = Constructors[PgbackrestClientDescriptor](5, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val name: CaseClassParm[PgbackrestClientDescriptor,String] = CaseClassParm[PgbackrestClientDescriptor,String]("name", _.name, (d,v) => d.copy(name = v), None, 0)
      lazy val pgdata: CaseClassParm[PgbackrestClientDescriptor,String] = CaseClassParm[PgbackrestClientDescriptor,String]("pgdata", _.pgdata, (d,v) => d.copy(pgdata = v), None, 1)
      lazy val stanzaNameOverride: CaseClassParm[PgbackrestClientDescriptor,Option[String]] = CaseClassParm[PgbackrestClientDescriptor,Option[String]]("stanzaNameOverride", _.stanzaNameOverride, (d,v) => d.copy(stanzaNameOverride = v), Some(()=> None), 2)
      lazy val onCalendar: CaseClassParm[PgbackrestClientDescriptor,Option[OnCalendarValue]] = CaseClassParm[PgbackrestClientDescriptor,Option[OnCalendarValue]]("onCalendar", _.onCalendar, (d,v) => d.copy(onCalendar = v), Some(()=> None), 3)
      lazy val configFile: CaseClassParm[PgbackrestClientDescriptor,Option[String]] = CaseClassParm[PgbackrestClientDescriptor,Option[String]]("configFile", _.configFile, (d,v) => d.copy(configFile = v), Some(()=> None), 4)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): PgbackrestClientDescriptor = {
        PgbackrestClientDescriptor(
          name = values(0).asInstanceOf[String],
          pgdata = values(1).asInstanceOf[String],
          stanzaNameOverride = values(2).asInstanceOf[Option[String]],
          onCalendar = values(3).asInstanceOf[Option[OnCalendarValue]],
          configFile = values(4).asInstanceOf[Option[String]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): PgbackrestClientDescriptor = {
        val value =
          PgbackrestClientDescriptor(
            name = values.next().asInstanceOf[String],
            pgdata = values.next().asInstanceOf[String],
            stanzaNameOverride = values.next().asInstanceOf[Option[String]],
            onCalendar = values.next().asInstanceOf[Option[OnCalendarValue]],
            configFile = values.next().asInstanceOf[Option[String]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(name: String, pgdata: String, stanzaNameOverride: Option[String], onCalendar: Option[OnCalendarValue], configFile: Option[String]): PgbackrestClientDescriptor =
        PgbackrestClientDescriptor(name, pgdata, stanzaNameOverride, onCalendar, configFile)
    
    }
    
    
    lazy val typeName = "PgbackrestClientDescriptor"
  
  }
  
  
  
  
  trait MxPgbackrestServerDescriptor {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[PgbackrestServerDescriptor,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[PgbackrestServerDescriptor,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[PgbackrestServerDescriptor,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.name)
          .addField(_.configHeader)
          .addField(_.configFile)
      )
      .build
    
    
    given scala.CanEqual[PgbackrestServerDescriptor, PgbackrestServerDescriptor] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[PgbackrestServerDescriptor,parameters.type] =  {
      val constructors = Constructors[PgbackrestServerDescriptor](3, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val name: CaseClassParm[PgbackrestServerDescriptor,String] = CaseClassParm[PgbackrestServerDescriptor,String]("name", _.name, (d,v) => d.copy(name = v), None, 0)
      lazy val configHeader: CaseClassParm[PgbackrestServerDescriptor,String] = CaseClassParm[PgbackrestServerDescriptor,String]("configHeader", _.configHeader, (d,v) => d.copy(configHeader = v), None, 1)
      lazy val configFile: CaseClassParm[PgbackrestServerDescriptor,Option[String]] = CaseClassParm[PgbackrestServerDescriptor,Option[String]]("configFile", _.configFile, (d,v) => d.copy(configFile = v), Some(()=> None), 2)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): PgbackrestServerDescriptor = {
        PgbackrestServerDescriptor(
          name = values(0).asInstanceOf[String],
          configHeader = values(1).asInstanceOf[String],
          configFile = values(2).asInstanceOf[Option[String]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): PgbackrestServerDescriptor = {
        val value =
          PgbackrestServerDescriptor(
            name = values.next().asInstanceOf[String],
            configHeader = values.next().asInstanceOf[String],
            configFile = values.next().asInstanceOf[Option[String]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(name: String, configHeader: String, configFile: Option[String]): PgbackrestServerDescriptor =
        PgbackrestServerDescriptor(name, configHeader, configFile)
    
    }
    
    
    lazy val typeName = "PgbackrestServerDescriptor"
  
  }
  
  
  
  
  trait MxServerDescriptor {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[ServerDescriptor,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[ServerDescriptor,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[ServerDescriptor,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.name)
          .addField(_.aliases)
          .addField(_.publicDomainName)
          .addField(_.vpnDomainName)
          .addField(_.users)
          .addField(_.a8VersionsExec)
          .addField(_.supervisorctlExec)
      )
      .build
    
    
    given scala.CanEqual[ServerDescriptor, ServerDescriptor] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[ServerDescriptor,parameters.type] =  {
      val constructors = Constructors[ServerDescriptor](7, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val name: CaseClassParm[ServerDescriptor,ServerName] = CaseClassParm[ServerDescriptor,ServerName]("name", _.name, (d,v) => d.copy(name = v), None, 0)
      lazy val aliases: CaseClassParm[ServerDescriptor,Iterable[ServerName]] = CaseClassParm[ServerDescriptor,Iterable[ServerName]]("aliases", _.aliases, (d,v) => d.copy(aliases = v), Some(()=> Iterable.empty), 1)
      lazy val publicDomainName: CaseClassParm[ServerDescriptor,Option[DomainName]] = CaseClassParm[ServerDescriptor,Option[DomainName]]("publicDomainName", _.publicDomainName, (d,v) => d.copy(publicDomainName = v), Some(()=> None), 2)
      lazy val vpnDomainName: CaseClassParm[ServerDescriptor,DomainName] = CaseClassParm[ServerDescriptor,DomainName]("vpnDomainName", _.vpnDomainName, (d,v) => d.copy(vpnDomainName = v), None, 3)
      lazy val users: CaseClassParm[ServerDescriptor,Vector[UserDescriptor]] = CaseClassParm[ServerDescriptor,Vector[UserDescriptor]]("users", _.users, (d,v) => d.copy(users = v), None, 4)
      lazy val a8VersionsExec: CaseClassParm[ServerDescriptor,Option[String]] = CaseClassParm[ServerDescriptor,Option[String]]("a8VersionsExec", _.a8VersionsExec, (d,v) => d.copy(a8VersionsExec = v), Some(()=> None), 5)
      lazy val supervisorctlExec: CaseClassParm[ServerDescriptor,Option[String]] = CaseClassParm[ServerDescriptor,Option[String]]("supervisorctlExec", _.supervisorctlExec, (d,v) => d.copy(supervisorctlExec = v), Some(()=> None), 6)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): ServerDescriptor = {
        ServerDescriptor(
          name = values(0).asInstanceOf[ServerName],
          aliases = values(1).asInstanceOf[Iterable[ServerName]],
          publicDomainName = values(2).asInstanceOf[Option[DomainName]],
          vpnDomainName = values(3).asInstanceOf[DomainName],
          users = values(4).asInstanceOf[Vector[UserDescriptor]],
          a8VersionsExec = values(5).asInstanceOf[Option[String]],
          supervisorctlExec = values(6).asInstanceOf[Option[String]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): ServerDescriptor = {
        val value =
          ServerDescriptor(
            name = values.next().asInstanceOf[ServerName],
            aliases = values.next().asInstanceOf[Iterable[ServerName]],
            publicDomainName = values.next().asInstanceOf[Option[DomainName]],
            vpnDomainName = values.next().asInstanceOf[DomainName],
            users = values.next().asInstanceOf[Vector[UserDescriptor]],
            a8VersionsExec = values.next().asInstanceOf[Option[String]],
            supervisorctlExec = values.next().asInstanceOf[Option[String]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(name: ServerName, aliases: Iterable[ServerName], publicDomainName: Option[DomainName], vpnDomainName: DomainName, users: Vector[UserDescriptor], a8VersionsExec: Option[String], supervisorctlExec: Option[String]): ServerDescriptor =
        ServerDescriptor(name, aliases, publicDomainName, vpnDomainName, users, a8VersionsExec, supervisorctlExec)
    
    }
    
    
    lazy val typeName = "ServerDescriptor"
  
  }
  
  
  
  
  trait MxAwsCredentials {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[AwsCredentials,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[AwsCredentials,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[AwsCredentials,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.awsSecretKey)
          .addField(_.awsAccessKey)
      )
      .build
    
    
    given scala.CanEqual[AwsCredentials, AwsCredentials] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[AwsCredentials,parameters.type] =  {
      val constructors = Constructors[AwsCredentials](2, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val awsSecretKey: CaseClassParm[AwsCredentials,AwsSecretKey] = CaseClassParm[AwsCredentials,AwsSecretKey]("awsSecretKey", _.awsSecretKey, (d,v) => d.copy(awsSecretKey = v), None, 0)
      lazy val awsAccessKey: CaseClassParm[AwsCredentials,AwsAccessKey] = CaseClassParm[AwsCredentials,AwsAccessKey]("awsAccessKey", _.awsAccessKey, (d,v) => d.copy(awsAccessKey = v), None, 1)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): AwsCredentials = {
        AwsCredentials(
          awsSecretKey = values(0).asInstanceOf[AwsSecretKey],
          awsAccessKey = values(1).asInstanceOf[AwsAccessKey],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): AwsCredentials = {
        val value =
          AwsCredentials(
            awsSecretKey = values.next().asInstanceOf[AwsSecretKey],
            awsAccessKey = values.next().asInstanceOf[AwsAccessKey],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(awsSecretKey: AwsSecretKey, awsAccessKey: AwsAccessKey): AwsCredentials =
        AwsCredentials(awsSecretKey, awsAccessKey)
    
    }
    
    
    lazy val typeName = "AwsCredentials"
  
  }
  
  
  
  
  trait MxManagedDomain {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[ManagedDomain,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[ManagedDomain,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[ManagedDomain,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.topLevelDomains)
          .addField(_.awsCredentials)
      )
      .build
    
    
    given scala.CanEqual[ManagedDomain, ManagedDomain] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[ManagedDomain,parameters.type] =  {
      val constructors = Constructors[ManagedDomain](2, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val topLevelDomains: CaseClassParm[ManagedDomain,Vector[DomainName]] = CaseClassParm[ManagedDomain,Vector[DomainName]]("topLevelDomains", _.topLevelDomains, (d,v) => d.copy(topLevelDomains = v), None, 0)
      lazy val awsCredentials: CaseClassParm[ManagedDomain,AwsCredentials] = CaseClassParm[ManagedDomain,AwsCredentials]("awsCredentials", _.awsCredentials, (d,v) => d.copy(awsCredentials = v), None, 1)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): ManagedDomain = {
        ManagedDomain(
          topLevelDomains = values(0).asInstanceOf[Vector[DomainName]],
          awsCredentials = values(1).asInstanceOf[AwsCredentials],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): ManagedDomain = {
        val value =
          ManagedDomain(
            topLevelDomains = values.next().asInstanceOf[Vector[DomainName]],
            awsCredentials = values.next().asInstanceOf[AwsCredentials],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(topLevelDomains: Vector[DomainName], awsCredentials: AwsCredentials): ManagedDomain =
        ManagedDomain(topLevelDomains, awsCredentials)
    
    }
    
    
    lazy val typeName = "ManagedDomain"
  
  }
  
  
  
  
  trait MxRepositoryDescriptor {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[RepositoryDescriptor,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[RepositoryDescriptor,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[RepositoryDescriptor,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.publicKeys)
          .addField(_.servers)
          .addField(_.healthchecksApiToken)
          .addField(_.managedDomains)
          .addField(_.plugins)
      )
      .build
    
    
    given scala.CanEqual[RepositoryDescriptor, RepositoryDescriptor] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[RepositoryDescriptor,parameters.type] =  {
      val constructors = Constructors[RepositoryDescriptor](5, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val publicKeys: CaseClassParm[RepositoryDescriptor,Iterable[Personnel]] = CaseClassParm[RepositoryDescriptor,Iterable[Personnel]]("publicKeys", _.publicKeys, (d,v) => d.copy(publicKeys = v), Some(()=> Iterable.empty), 0)
      lazy val servers: CaseClassParm[RepositoryDescriptor,Vector[ServerDescriptor]] = CaseClassParm[RepositoryDescriptor,Vector[ServerDescriptor]]("servers", _.servers, (d,v) => d.copy(servers = v), None, 1)
      lazy val healthchecksApiToken: CaseClassParm[RepositoryDescriptor,HealthchecksDotIo.ApiAuthToken] = CaseClassParm[RepositoryDescriptor,HealthchecksDotIo.ApiAuthToken]("healthchecksApiToken", _.healthchecksApiToken, (d,v) => d.copy(healthchecksApiToken = v), None, 2)
      lazy val managedDomains: CaseClassParm[RepositoryDescriptor,Vector[ManagedDomain]] = CaseClassParm[RepositoryDescriptor,Vector[ManagedDomain]]("managedDomains", _.managedDomains, (d,v) => d.copy(managedDomains = v), Some(()=> Vector.empty), 3)
      lazy val plugins: CaseClassParm[RepositoryDescriptor,JsDoc] = CaseClassParm[RepositoryDescriptor,JsDoc]("plugins", _.plugins, (d,v) => d.copy(plugins = v), Some(()=> JsDoc.empty), 4)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): RepositoryDescriptor = {
        RepositoryDescriptor(
          publicKeys = values(0).asInstanceOf[Iterable[Personnel]],
          servers = values(1).asInstanceOf[Vector[ServerDescriptor]],
          healthchecksApiToken = values(2).asInstanceOf[HealthchecksDotIo.ApiAuthToken],
          managedDomains = values(3).asInstanceOf[Vector[ManagedDomain]],
          plugins = values(4).asInstanceOf[JsDoc],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): RepositoryDescriptor = {
        val value =
          RepositoryDescriptor(
            publicKeys = values.next().asInstanceOf[Iterable[Personnel]],
            servers = values.next().asInstanceOf[Vector[ServerDescriptor]],
            healthchecksApiToken = values.next().asInstanceOf[HealthchecksDotIo.ApiAuthToken],
            managedDomains = values.next().asInstanceOf[Vector[ManagedDomain]],
            plugins = values.next().asInstanceOf[JsDoc],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(publicKeys: Iterable[Personnel], servers: Vector[ServerDescriptor], healthchecksApiToken: HealthchecksDotIo.ApiAuthToken, managedDomains: Vector[ManagedDomain], plugins: JsDoc): RepositoryDescriptor =
        RepositoryDescriptor(publicKeys, servers, healthchecksApiToken, managedDomains, plugins)
    
    }
    
    
    lazy val typeName = "RepositoryDescriptor"
  
  }
  
  
  
  
  trait MxPersonnel {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[Personnel,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[Personnel,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[Personnel,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.id)
          .addField(_.description)
          .addField(_.authorizedKeysUrl)
          .addField(_.authorizedKeys)
          .addField(_.members)
      )
      .build
    
    
    given scala.CanEqual[Personnel, Personnel] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[Personnel,parameters.type] =  {
      val constructors = Constructors[Personnel](5, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val id: CaseClassParm[Personnel,QualifiedUserName] = CaseClassParm[Personnel,QualifiedUserName]("id", _.id, (d,v) => d.copy(id = v), None, 0)
      lazy val description: CaseClassParm[Personnel,String] = CaseClassParm[Personnel,String]("description", _.description, (d,v) => d.copy(description = v), None, 1)
      lazy val authorizedKeysUrl: CaseClassParm[Personnel,Option[String]] = CaseClassParm[Personnel,Option[String]]("authorizedKeysUrl", _.authorizedKeysUrl, (d,v) => d.copy(authorizedKeysUrl = v), Some(()=> None), 2)
      lazy val authorizedKeys: CaseClassParm[Personnel,Iterable[AuthorizedKey]] = CaseClassParm[Personnel,Iterable[AuthorizedKey]]("authorizedKeys", _.authorizedKeys, (d,v) => d.copy(authorizedKeys = v), Some(()=> None), 3)
      lazy val members: CaseClassParm[Personnel,Iterable[QualifiedUserName]] = CaseClassParm[Personnel,Iterable[QualifiedUserName]]("members", _.members, (d,v) => d.copy(members = v), Some(()=> Iterable.empty), 4)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): Personnel = {
        Personnel(
          id = values(0).asInstanceOf[QualifiedUserName],
          description = values(1).asInstanceOf[String],
          authorizedKeysUrl = values(2).asInstanceOf[Option[String]],
          authorizedKeys = values(3).asInstanceOf[Iterable[AuthorizedKey]],
          members = values(4).asInstanceOf[Iterable[QualifiedUserName]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): Personnel = {
        val value =
          Personnel(
            id = values.next().asInstanceOf[QualifiedUserName],
            description = values.next().asInstanceOf[String],
            authorizedKeysUrl = values.next().asInstanceOf[Option[String]],
            authorizedKeys = values.next().asInstanceOf[Iterable[AuthorizedKey]],
            members = values.next().asInstanceOf[Iterable[QualifiedUserName]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(id: QualifiedUserName, description: String, authorizedKeysUrl: Option[String], authorizedKeys: Iterable[AuthorizedKey], members: Iterable[QualifiedUserName]): Personnel =
        Personnel(id, description, authorizedKeysUrl, authorizedKeys, members)
    
    }
    
    
    lazy val typeName = "Personnel"
  
  }
}
