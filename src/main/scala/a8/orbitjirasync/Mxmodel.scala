package a8.orbitjirasync

/**

  WARNING THIS IS GENERATED CODE.  DO NOT EDIT.

  The only manually maintained code is the code between the //==== (normally where you add your imports)

*/

//====
import a8.sync.qubes.QubesApiClient
import a8.orbitjirasync.model._
import a8.shared.jdbcf.DatabaseConfig.Password
import sttp.model.Uri
//====

import a8.shared.Meta.{CaseClassParm, Generator, Constructors}



object Mxmodel {
  
  trait MxTask {
  
    implicit lazy val qubesMapper: a8.sync.qubes.QubesKeyedMapper[Task,String] =
      a8.sync.qubes.QubesMapperBuilder(generator)
        .addField(_.uid)
        .addField(_.name)
        .addField(_.description)
        .addField(_.visible)
        .addField(_.jiraTicket)
        .addField(_.projectUid)    
        .cubeName("Task")
        .appSpace("reaper")
        .singlePrimaryKey(_.uid)
        .build
    
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[Task,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[Task,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[Task,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.uid)
          .addField(_.name)
          .addField(_.description)
          .addField(_.visible)
          .addField(_.jiraTicket)
          .addField(_.projectUid)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[Task] = zio.prelude.Equal.default
    
    implicit val catsEq: cats.Eq[Task] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[Task,parameters.type] =  {
      val constructors = Constructors[Task](6, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val uid: CaseClassParm[Task,String] = CaseClassParm[Task,String]("uid", _.uid, (d,v) => d.copy(uid = v), None, 0)
      lazy val name: CaseClassParm[Task,String] = CaseClassParm[Task,String]("name", _.name, (d,v) => d.copy(name = v), None, 1)
      lazy val description: CaseClassParm[Task,String] = CaseClassParm[Task,String]("description", _.description, (d,v) => d.copy(description = v), None, 2)
      lazy val visible: CaseClassParm[Task,Boolean] = CaseClassParm[Task,Boolean]("visible", _.visible, (d,v) => d.copy(visible = v), None, 3)
      lazy val jiraTicket: CaseClassParm[Task,Option[String]] = CaseClassParm[Task,Option[String]]("jiraTicket", _.jiraTicket, (d,v) => d.copy(jiraTicket = v), Some(()=> None), 4)
      lazy val projectUid: CaseClassParm[Task,Option[OrbitProjectUid]] = CaseClassParm[Task,Option[OrbitProjectUid]]("projectUid", _.projectUid, (d,v) => d.copy(projectUid = v), Some(()=> None), 5)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): Task = {
        Task(
          uid = values(0).asInstanceOf[String],
          name = values(1).asInstanceOf[String],
          description = values(2).asInstanceOf[String],
          visible = values(3).asInstanceOf[Boolean],
          jiraTicket = values(4).asInstanceOf[Option[String]],
          projectUid = values(5).asInstanceOf[Option[OrbitProjectUid]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): Task = {
        val value =
          Task(
            uid = values.next().asInstanceOf[String],
            name = values.next().asInstanceOf[String],
            description = values.next().asInstanceOf[String],
            visible = values.next().asInstanceOf[Boolean],
            jiraTicket = values.next().asInstanceOf[Option[String]],
            projectUid = values.next().asInstanceOf[Option[OrbitProjectUid]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(uid: String, name: String, description: String, visible: Boolean, jiraTicket: Option[String], projectUid: Option[OrbitProjectUid]): Task =
        Task(uid, name, description, visible, jiraTicket, projectUid)
    
    }
    
    
    lazy val typeName = "Task"
  
  }
  
  
  
  
  trait MxConfig {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[Config,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[Config,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[Config,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.jiraSyncs)
          .addField(_.qubes)
          .addField(_.readOnly)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[Config] = zio.prelude.Equal.default
    
    implicit val catsEq: cats.Eq[Config] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[Config,parameters.type] =  {
      val constructors = Constructors[Config](3, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val jiraSyncs: CaseClassParm[Config,Vector[JiraSync]] = CaseClassParm[Config,Vector[JiraSync]]("jiraSyncs", _.jiraSyncs, (d,v) => d.copy(jiraSyncs = v), None, 0)
      lazy val qubes: CaseClassParm[Config,QubesApiClient.Config] = CaseClassParm[Config,QubesApiClient.Config]("qubes", _.qubes, (d,v) => d.copy(qubes = v), None, 1)
      lazy val readOnly: CaseClassParm[Config,Boolean] = CaseClassParm[Config,Boolean]("readOnly", _.readOnly, (d,v) => d.copy(readOnly = v), Some(()=> true), 2)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): Config = {
        Config(
          jiraSyncs = values(0).asInstanceOf[Vector[JiraSync]],
          qubes = values(1).asInstanceOf[QubesApiClient.Config],
          readOnly = values(2).asInstanceOf[Boolean],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): Config = {
        val value =
          Config(
            jiraSyncs = values.next().asInstanceOf[Vector[JiraSync]],
            qubes = values.next().asInstanceOf[QubesApiClient.Config],
            readOnly = values.next().asInstanceOf[Boolean],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(jiraSyncs: Vector[JiraSync], qubes: QubesApiClient.Config, readOnly: Boolean): Config =
        Config(jiraSyncs, qubes, readOnly)
    
    }
    
    
    lazy val typeName = "Config"
  
  }
  
  
  
  
  trait MxJiraSync {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[JiraSync,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[JiraSync,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[JiraSync,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.jiraConfig)
          .addField(_.boardMappings)
          .addField(_.jqlMappings)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[JiraSync] = zio.prelude.Equal.default
    
    implicit val catsEq: cats.Eq[JiraSync] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[JiraSync,parameters.type] =  {
      val constructors = Constructors[JiraSync](3, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val jiraConfig: CaseClassParm[JiraSync,JiraConfig] = CaseClassParm[JiraSync,JiraConfig]("jiraConfig", _.jiraConfig, (d,v) => d.copy(jiraConfig = v), None, 0)
      lazy val boardMappings: CaseClassParm[JiraSync,Vector[JiraBoardMapping]] = CaseClassParm[JiraSync,Vector[JiraBoardMapping]]("boardMappings", _.boardMappings, (d,v) => d.copy(boardMappings = v), Some(()=> Vector.empty), 1)
      lazy val jqlMappings: CaseClassParm[JiraSync,Vector[JqlToProjectMapping]] = CaseClassParm[JiraSync,Vector[JqlToProjectMapping]]("jqlMappings", _.jqlMappings, (d,v) => d.copy(jqlMappings = v), Some(()=> Vector.empty), 2)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): JiraSync = {
        JiraSync(
          jiraConfig = values(0).asInstanceOf[JiraConfig],
          boardMappings = values(1).asInstanceOf[Vector[JiraBoardMapping]],
          jqlMappings = values(2).asInstanceOf[Vector[JqlToProjectMapping]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): JiraSync = {
        val value =
          JiraSync(
            jiraConfig = values.next().asInstanceOf[JiraConfig],
            boardMappings = values.next().asInstanceOf[Vector[JiraBoardMapping]],
            jqlMappings = values.next().asInstanceOf[Vector[JqlToProjectMapping]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(jiraConfig: JiraConfig, boardMappings: Vector[JiraBoardMapping], jqlMappings: Vector[JqlToProjectMapping]): JiraSync =
        JiraSync(jiraConfig, boardMappings, jqlMappings)
    
    }
    
    
    lazy val typeName = "JiraSync"
  
  }
  
  
  
  
  trait MxJiraConfig {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[JiraConfig,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[JiraConfig,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[JiraConfig,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.serverRootUrl)
          .addField(_.user)
          .addField(_.password)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[JiraConfig] = zio.prelude.Equal.default
    
    implicit val catsEq: cats.Eq[JiraConfig] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[JiraConfig,parameters.type] =  {
      val constructors = Constructors[JiraConfig](3, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val serverRootUrl: CaseClassParm[JiraConfig,Uri] = CaseClassParm[JiraConfig,Uri]("serverRootUrl", _.serverRootUrl, (d,v) => d.copy(serverRootUrl = v), None, 0)
      lazy val user: CaseClassParm[JiraConfig,String] = CaseClassParm[JiraConfig,String]("user", _.user, (d,v) => d.copy(user = v), None, 1)
      lazy val password: CaseClassParm[JiraConfig,Password] = CaseClassParm[JiraConfig,Password]("password", _.password, (d,v) => d.copy(password = v), None, 2)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): JiraConfig = {
        JiraConfig(
          serverRootUrl = values(0).asInstanceOf[Uri],
          user = values(1).asInstanceOf[String],
          password = values(2).asInstanceOf[Password],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): JiraConfig = {
        val value =
          JiraConfig(
            serverRootUrl = values.next().asInstanceOf[Uri],
            user = values.next().asInstanceOf[String],
            password = values.next().asInstanceOf[Password],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(serverRootUrl: Uri, user: String, password: Password): JiraConfig =
        JiraConfig(serverRootUrl, user, password)
    
    }
    
    
    lazy val typeName = "JiraConfig"
  
  }
  
  
  
  
  trait MxEpicToProjectMapping {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[EpicToProjectMapping,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[EpicToProjectMapping,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[EpicToProjectMapping,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.epicKey)
          .addField(_.orbitProjectUid)
          .addField(_.description)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[EpicToProjectMapping] = zio.prelude.Equal.default
    
    implicit val catsEq: cats.Eq[EpicToProjectMapping] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[EpicToProjectMapping,parameters.type] =  {
      val constructors = Constructors[EpicToProjectMapping](3, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val epicKey: CaseClassParm[EpicToProjectMapping,Option[IssueKey]] = CaseClassParm[EpicToProjectMapping,Option[IssueKey]]("epicKey", _.epicKey, (d,v) => d.copy(epicKey = v), Some(()=> None), 0)
      lazy val orbitProjectUid: CaseClassParm[EpicToProjectMapping,OrbitProjectUid] = CaseClassParm[EpicToProjectMapping,OrbitProjectUid]("orbitProjectUid", _.orbitProjectUid, (d,v) => d.copy(orbitProjectUid = v), None, 1)
      lazy val description: CaseClassParm[EpicToProjectMapping,Option[String]] = CaseClassParm[EpicToProjectMapping,Option[String]]("description", _.description, (d,v) => d.copy(description = v), Some(()=> None), 2)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): EpicToProjectMapping = {
        EpicToProjectMapping(
          epicKey = values(0).asInstanceOf[Option[IssueKey]],
          orbitProjectUid = values(1).asInstanceOf[OrbitProjectUid],
          description = values(2).asInstanceOf[Option[String]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): EpicToProjectMapping = {
        val value =
          EpicToProjectMapping(
            epicKey = values.next().asInstanceOf[Option[IssueKey]],
            orbitProjectUid = values.next().asInstanceOf[OrbitProjectUid],
            description = values.next().asInstanceOf[Option[String]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(epicKey: Option[IssueKey], orbitProjectUid: OrbitProjectUid, description: Option[String]): EpicToProjectMapping =
        EpicToProjectMapping(epicKey, orbitProjectUid, description)
    
    }
    
    
    lazy val typeName = "EpicToProjectMapping"
  
  }
  
  
  
  
  trait MxJiraBoardMapping {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[JiraBoardMapping,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[JiraBoardMapping,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[JiraBoardMapping,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.jiraBoardId)
          .addField(_.mappings)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[JiraBoardMapping] = zio.prelude.Equal.default
    
    implicit val catsEq: cats.Eq[JiraBoardMapping] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[JiraBoardMapping,parameters.type] =  {
      val constructors = Constructors[JiraBoardMapping](2, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val jiraBoardId: CaseClassParm[JiraBoardMapping,JiraBoardId] = CaseClassParm[JiraBoardMapping,JiraBoardId]("jiraBoardId", _.jiraBoardId, (d,v) => d.copy(jiraBoardId = v), None, 0)
      lazy val mappings: CaseClassParm[JiraBoardMapping,Vector[EpicToProjectMapping]] = CaseClassParm[JiraBoardMapping,Vector[EpicToProjectMapping]]("mappings", _.mappings, (d,v) => d.copy(mappings = v), None, 1)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): JiraBoardMapping = {
        JiraBoardMapping(
          jiraBoardId = values(0).asInstanceOf[JiraBoardId],
          mappings = values(1).asInstanceOf[Vector[EpicToProjectMapping]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): JiraBoardMapping = {
        val value =
          JiraBoardMapping(
            jiraBoardId = values.next().asInstanceOf[JiraBoardId],
            mappings = values.next().asInstanceOf[Vector[EpicToProjectMapping]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(jiraBoardId: JiraBoardId, mappings: Vector[EpicToProjectMapping]): JiraBoardMapping =
        JiraBoardMapping(jiraBoardId, mappings)
    
    }
    
    
    lazy val typeName = "JiraBoardMapping"
  
  }
  
  
  
  
  trait MxJqlToProjectMapping {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[JqlToProjectMapping,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[JqlToProjectMapping,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[JqlToProjectMapping,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.jql)
          .addField(_.orbitProjectUid)
          .addField(_.description)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[JqlToProjectMapping] = zio.prelude.Equal.default
    
    implicit val catsEq: cats.Eq[JqlToProjectMapping] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[JqlToProjectMapping,parameters.type] =  {
      val constructors = Constructors[JqlToProjectMapping](3, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val jql: CaseClassParm[JqlToProjectMapping,String] = CaseClassParm[JqlToProjectMapping,String]("jql", _.jql, (d,v) => d.copy(jql = v), None, 0)
      lazy val orbitProjectUid: CaseClassParm[JqlToProjectMapping,OrbitProjectUid] = CaseClassParm[JqlToProjectMapping,OrbitProjectUid]("orbitProjectUid", _.orbitProjectUid, (d,v) => d.copy(orbitProjectUid = v), None, 1)
      lazy val description: CaseClassParm[JqlToProjectMapping,Option[String]] = CaseClassParm[JqlToProjectMapping,Option[String]]("description", _.description, (d,v) => d.copy(description = v), Some(()=> None), 2)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): JqlToProjectMapping = {
        JqlToProjectMapping(
          jql = values(0).asInstanceOf[String],
          orbitProjectUid = values(1).asInstanceOf[OrbitProjectUid],
          description = values(2).asInstanceOf[Option[String]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): JqlToProjectMapping = {
        val value =
          JqlToProjectMapping(
            jql = values.next().asInstanceOf[String],
            orbitProjectUid = values.next().asInstanceOf[OrbitProjectUid],
            description = values.next().asInstanceOf[Option[String]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(jql: String, orbitProjectUid: OrbitProjectUid, description: Option[String]): JqlToProjectMapping =
        JqlToProjectMapping(jql, orbitProjectUid, description)
    
    }
    
    
    lazy val typeName = "JqlToProjectMapping"
  
  }
}
