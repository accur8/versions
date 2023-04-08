package io.accur8.neodeploy.systemstate

/**

  WARNING THIS IS GENERATED CODE.  DO NOT EDIT.

  The only manually maintained code is the code between the //==== (normally where you add your imports)

*/

//====
import a8.shared.ZFileSystem
import io.accur8.neodeploy.HealthchecksDotIo
import io.accur8.neodeploy.model.{ApplicationDescriptor, DockerDescriptor, DomainName}
import io.accur8.neodeploy.model.Install.JavaApp
import io.accur8.neodeploy.systemstate.SystemState._
import io.accur8.neodeploy.systemstate.SystemStateModel._
//====

import a8.shared.Meta.{CaseClassParm, Generator, Constructors}



object MxSystemState {
  
  trait MxTextFile {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[TextFile,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[TextFile,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[TextFile,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.file)
          .addField(_.contents)
          .addField(_.perms)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[TextFile] = zio.prelude.Equal.default
    
    
    
    
    lazy val generator: Generator[TextFile,parameters.type] =  {
      val constructors = Constructors[TextFile](3, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val file: CaseClassParm[TextFile,ZFileSystem.File] = CaseClassParm[TextFile,ZFileSystem.File]("file", _.file, (d,v) => d.copy(file = v), None, 0)
      lazy val contents: CaseClassParm[TextFile,String] = CaseClassParm[TextFile,String]("contents", _.contents, (d,v) => d.copy(contents = v), None, 1)
      lazy val perms: CaseClassParm[TextFile,UnixPerms] = CaseClassParm[TextFile,UnixPerms]("perms", _.perms, (d,v) => d.copy(perms = v), Some(()=> UnixPerms.empty), 2)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): TextFile = {
        TextFile(
          file = values(0).asInstanceOf[ZFileSystem.File],
          contents = values(1).asInstanceOf[String],
          perms = values(2).asInstanceOf[UnixPerms],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): TextFile = {
        val value =
          TextFile(
            file = values.next().asInstanceOf[ZFileSystem.File],
            contents = values.next().asInstanceOf[String],
            perms = values.next().asInstanceOf[UnixPerms],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(file: ZFileSystem.File, contents: String, perms: UnixPerms): TextFile =
        TextFile(file, contents, perms)
    
    }
    
    
    lazy val typeName = "TextFile"
  
  }
  
  
  
  
  trait MxSecretsTextFile {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[SecretsTextFile,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[SecretsTextFile,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[SecretsTextFile,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.file)
          .addField(_.secretContents)
          .addField(_.perms)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[SecretsTextFile] = zio.prelude.Equal.default
    
    
    
    
    lazy val generator: Generator[SecretsTextFile,parameters.type] =  {
      val constructors = Constructors[SecretsTextFile](3, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val file: CaseClassParm[SecretsTextFile,ZFileSystem.File] = CaseClassParm[SecretsTextFile,ZFileSystem.File]("file", _.file, (d,v) => d.copy(file = v), None, 0)
      lazy val secretContents: CaseClassParm[SecretsTextFile,SecretContent] = CaseClassParm[SecretsTextFile,SecretContent]("secretContents", _.secretContents, (d,v) => d.copy(secretContents = v), None, 1)
      lazy val perms: CaseClassParm[SecretsTextFile,UnixPerms] = CaseClassParm[SecretsTextFile,UnixPerms]("perms", _.perms, (d,v) => d.copy(perms = v), Some(()=> UnixPerms.empty), 2)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): SecretsTextFile = {
        SecretsTextFile(
          file = values(0).asInstanceOf[ZFileSystem.File],
          secretContents = values(1).asInstanceOf[SecretContent],
          perms = values(2).asInstanceOf[UnixPerms],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): SecretsTextFile = {
        val value =
          SecretsTextFile(
            file = values.next().asInstanceOf[ZFileSystem.File],
            secretContents = values.next().asInstanceOf[SecretContent],
            perms = values.next().asInstanceOf[UnixPerms],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(file: ZFileSystem.File, secretContents: SecretContent, perms: UnixPerms): SecretsTextFile =
        SecretsTextFile(file, secretContents, perms)
    
    }
    
    
    lazy val typeName = "SecretsTextFile"
  
  }
  
  
  
  
  trait MxJavaAppInstall {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[JavaAppInstall,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[JavaAppInstall,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[JavaAppInstall,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.appInstallDir)
          .addField(_.fromRepo)
          .addField(_.descriptor)
          .addField(_.gitAppDirectory)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[JavaAppInstall] = zio.prelude.Equal.default
    
    
    
    
    lazy val generator: Generator[JavaAppInstall,parameters.type] =  {
      val constructors = Constructors[JavaAppInstall](4, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val appInstallDir: CaseClassParm[JavaAppInstall,ZFileSystem.Directory] = CaseClassParm[JavaAppInstall,ZFileSystem.Directory]("appInstallDir", _.appInstallDir, (d,v) => d.copy(appInstallDir = v), None, 0)
      lazy val fromRepo: CaseClassParm[JavaAppInstall,JavaApp] = CaseClassParm[JavaAppInstall,JavaApp]("fromRepo", _.fromRepo, (d,v) => d.copy(fromRepo = v), None, 1)
      lazy val descriptor: CaseClassParm[JavaAppInstall,ApplicationDescriptor] = CaseClassParm[JavaAppInstall,ApplicationDescriptor]("descriptor", _.descriptor, (d,v) => d.copy(descriptor = v), None, 2)
      lazy val gitAppDirectory: CaseClassParm[JavaAppInstall,ZFileSystem.Directory] = CaseClassParm[JavaAppInstall,ZFileSystem.Directory]("gitAppDirectory", _.gitAppDirectory, (d,v) => d.copy(gitAppDirectory = v), None, 3)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): JavaAppInstall = {
        JavaAppInstall(
          appInstallDir = values(0).asInstanceOf[ZFileSystem.Directory],
          fromRepo = values(1).asInstanceOf[JavaApp],
          descriptor = values(2).asInstanceOf[ApplicationDescriptor],
          gitAppDirectory = values(3).asInstanceOf[ZFileSystem.Directory],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): JavaAppInstall = {
        val value =
          JavaAppInstall(
            appInstallDir = values.next().asInstanceOf[ZFileSystem.Directory],
            fromRepo = values.next().asInstanceOf[JavaApp],
            descriptor = values.next().asInstanceOf[ApplicationDescriptor],
            gitAppDirectory = values.next().asInstanceOf[ZFileSystem.Directory],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(appInstallDir: ZFileSystem.Directory, fromRepo: JavaApp, descriptor: ApplicationDescriptor, gitAppDirectory: ZFileSystem.Directory): JavaAppInstall =
        JavaAppInstall(appInstallDir, fromRepo, descriptor, gitAppDirectory)
    
    }
    
    
    lazy val typeName = "JavaAppInstall"
  
  }
  
  
  
  
  trait MxDirectory {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[Directory,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[Directory,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[Directory,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.path)
          .addField(_.perms)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[Directory] = zio.prelude.Equal.default
    
    
    
    
    lazy val generator: Generator[Directory,parameters.type] =  {
      val constructors = Constructors[Directory](2, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val path: CaseClassParm[Directory,ZFileSystem.Directory] = CaseClassParm[Directory,ZFileSystem.Directory]("path", _.path, (d,v) => d.copy(path = v), None, 0)
      lazy val perms: CaseClassParm[Directory,UnixPerms] = CaseClassParm[Directory,UnixPerms]("perms", _.perms, (d,v) => d.copy(perms = v), Some(()=> UnixPerms.empty), 1)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): Directory = {
        Directory(
          path = values(0).asInstanceOf[ZFileSystem.Directory],
          perms = values(1).asInstanceOf[UnixPerms],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): Directory = {
        val value =
          Directory(
            path = values.next().asInstanceOf[ZFileSystem.Directory],
            perms = values.next().asInstanceOf[UnixPerms],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(path: ZFileSystem.Directory, perms: UnixPerms): Directory =
        Directory(path, perms)
    
    }
    
    
    lazy val typeName = "Directory"
  
  }
  
  
  
  
  trait MxComposite {
  
    implicit val zioEq: zio.prelude.Equal[Composite] = zio.prelude.Equal.default
    
    
    
    
    lazy val generator: Generator[Composite,parameters.type] =  {
      val constructors = Constructors[Composite](2, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val description: CaseClassParm[Composite,String] = CaseClassParm[Composite,String]("description", _.description, (d,v) => d.copy(description = v), None, 0)
      lazy val states: CaseClassParm[Composite,Vector[SystemState]] = CaseClassParm[Composite,Vector[SystemState]]("states", _.states, (d,v) => d.copy(states = v), None, 1)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): Composite = {
        Composite(
          description = values(0).asInstanceOf[String],
          states = values(1).asInstanceOf[Vector[SystemState]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): Composite = {
        val value =
          Composite(
            description = values.next().asInstanceOf[String],
            states = values.next().asInstanceOf[Vector[SystemState]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(description: String, states: Vector[SystemState]): Composite =
        Composite(description, states)
    
    }
    
    
    lazy val typeName = "Composite"
  
  }
  
  
  
  
  trait MxDnsRecord {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[DnsRecord,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[DnsRecord,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[DnsRecord,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.name)
          .addField(_.recordType)
          .addField(_.values)
          .addField(_.ttl)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[DnsRecord] = zio.prelude.Equal.default
    
    
    
    
    lazy val generator: Generator[DnsRecord,parameters.type] =  {
      val constructors = Constructors[DnsRecord](4, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val name: CaseClassParm[DnsRecord,DomainName] = CaseClassParm[DnsRecord,DomainName]("name", _.name, (d,v) => d.copy(name = v), None, 0)
      lazy val recordType: CaseClassParm[DnsRecord,String] = CaseClassParm[DnsRecord,String]("recordType", _.recordType, (d,v) => d.copy(recordType = v), None, 1)
      lazy val values: CaseClassParm[DnsRecord,Vector[String]] = CaseClassParm[DnsRecord,Vector[String]]("values", _.values, (d,v) => d.copy(values = v), None, 2)
      lazy val ttl: CaseClassParm[DnsRecord,Long] = CaseClassParm[DnsRecord,Long]("ttl", _.ttl, (d,v) => d.copy(ttl = v), None, 3)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): DnsRecord = {
        DnsRecord(
          name = values(0).asInstanceOf[DomainName],
          recordType = values(1).asInstanceOf[String],
          values = values(2).asInstanceOf[Vector[String]],
          ttl = values(3).asInstanceOf[Long],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): DnsRecord = {
        val value =
          DnsRecord(
            name = values.next().asInstanceOf[DomainName],
            recordType = values.next().asInstanceOf[String],
            values = values.next().asInstanceOf[Vector[String]],
            ttl = values.next().asInstanceOf[Long],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(name: DomainName, recordType: String, values: Vector[String], ttl: Long): DnsRecord =
        DnsRecord(name, recordType, values, ttl)
    
    }
    
    
    lazy val typeName = "DnsRecord"
  
  }
  
  
  
  
  trait MxHealthCheck {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[HealthCheck,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[HealthCheck,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[HealthCheck,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.data)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[HealthCheck] = zio.prelude.Equal.default
    
    
    
    
    lazy val generator: Generator[HealthCheck,parameters.type] =  {
      val constructors = Constructors[HealthCheck](1, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val data: CaseClassParm[HealthCheck,HealthchecksDotIo.CheckUpsertRequest] = CaseClassParm[HealthCheck,HealthchecksDotIo.CheckUpsertRequest]("data", _.data, (d,v) => d.copy(data = v), None, 0)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): HealthCheck = {
        HealthCheck(
          data = values(0).asInstanceOf[HealthchecksDotIo.CheckUpsertRequest],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): HealthCheck = {
        val value =
          HealthCheck(
            data = values.next().asInstanceOf[HealthchecksDotIo.CheckUpsertRequest],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(data: HealthchecksDotIo.CheckUpsertRequest): HealthCheck =
        HealthCheck(data)
    
    }
    
    
    lazy val typeName = "HealthCheck"
  
  }
  
  
  
  
  trait MxRunCommandState {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[RunCommandState,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[RunCommandState,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[RunCommandState,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.stateKey)
          .addField(_.installCommands)
          .addField(_.uninstallCommands)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[RunCommandState] = zio.prelude.Equal.default
    
    
    
    
    lazy val generator: Generator[RunCommandState,parameters.type] =  {
      val constructors = Constructors[RunCommandState](3, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val stateKey: CaseClassParm[RunCommandState,Option[StateKey]] = CaseClassParm[RunCommandState,Option[StateKey]]("stateKey", _.stateKey, (d,v) => d.copy(stateKey = v), Some(()=> None), 0)
      lazy val installCommands: CaseClassParm[RunCommandState,Vector[Command]] = CaseClassParm[RunCommandState,Vector[Command]]("installCommands", _.installCommands, (d,v) => d.copy(installCommands = v), Some(()=> Vector.empty), 1)
      lazy val uninstallCommands: CaseClassParm[RunCommandState,Vector[Command]] = CaseClassParm[RunCommandState,Vector[Command]]("uninstallCommands", _.uninstallCommands, (d,v) => d.copy(uninstallCommands = v), Some(()=> Vector.empty), 2)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): RunCommandState = {
        RunCommandState(
          stateKey = values(0).asInstanceOf[Option[StateKey]],
          installCommands = values(1).asInstanceOf[Vector[Command]],
          uninstallCommands = values(2).asInstanceOf[Vector[Command]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): RunCommandState = {
        val value =
          RunCommandState(
            stateKey = values.next().asInstanceOf[Option[StateKey]],
            installCommands = values.next().asInstanceOf[Vector[Command]],
            uninstallCommands = values.next().asInstanceOf[Vector[Command]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(stateKey: Option[StateKey], installCommands: Vector[Command], uninstallCommands: Vector[Command]): RunCommandState =
        RunCommandState(stateKey, installCommands, uninstallCommands)
    
    }
    
    
    lazy val typeName = "RunCommandState"
  
  }
  
  
  
  
  trait MxDockerState {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[DockerState,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[DockerState,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[DockerState,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.descriptor)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[DockerState] = zio.prelude.Equal.default
    
    
    
    
    lazy val generator: Generator[DockerState,parameters.type] =  {
      val constructors = Constructors[DockerState](1, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val descriptor: CaseClassParm[DockerState,DockerDescriptor] = CaseClassParm[DockerState,DockerDescriptor]("descriptor", _.descriptor, (d,v) => d.copy(descriptor = v), None, 0)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): DockerState = {
        DockerState(
          descriptor = values(0).asInstanceOf[DockerDescriptor],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): DockerState = {
        val value =
          DockerState(
            descriptor = values.next().asInstanceOf[DockerDescriptor],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(descriptor: DockerDescriptor): DockerState =
        DockerState(descriptor)
    
    }
    
    
    lazy val typeName = "DockerState"
  
  }
  
  
  
  
  trait MxTriggeredState {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[TriggeredState,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[TriggeredState,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[TriggeredState,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.preTriggerState)
          .addField(_.postTriggerState)
          .addField(_.triggerState)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[TriggeredState] = zio.prelude.Equal.default
    
    
    
    
    lazy val generator: Generator[TriggeredState,parameters.type] =  {
      val constructors = Constructors[TriggeredState](3, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val preTriggerState: CaseClassParm[TriggeredState,SystemState] = CaseClassParm[TriggeredState,SystemState]("preTriggerState", _.preTriggerState, (d,v) => d.copy(preTriggerState = v), Some(()=> SystemState.Empty), 0)
      lazy val postTriggerState: CaseClassParm[TriggeredState,SystemState] = CaseClassParm[TriggeredState,SystemState]("postTriggerState", _.postTriggerState, (d,v) => d.copy(postTriggerState = v), Some(()=> SystemState.Empty), 1)
      lazy val triggerState: CaseClassParm[TriggeredState,SystemState] = CaseClassParm[TriggeredState,SystemState]("triggerState", _.triggerState, (d,v) => d.copy(triggerState = v), Some(()=> SystemState.Empty), 2)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): TriggeredState = {
        TriggeredState(
          preTriggerState = values(0).asInstanceOf[SystemState],
          postTriggerState = values(1).asInstanceOf[SystemState],
          triggerState = values(2).asInstanceOf[SystemState],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): TriggeredState = {
        val value =
          TriggeredState(
            preTriggerState = values.next().asInstanceOf[SystemState],
            postTriggerState = values.next().asInstanceOf[SystemState],
            triggerState = values.next().asInstanceOf[SystemState],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(preTriggerState: SystemState, postTriggerState: SystemState, triggerState: SystemState): TriggeredState =
        TriggeredState(preTriggerState, postTriggerState, triggerState)
    
    }
    
    
    lazy val typeName = "TriggeredState"
  
  }
}
