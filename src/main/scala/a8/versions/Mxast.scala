package a8.versions

import a8.shared.Meta.{CaseClassParm, Generator, Constructors}

/**

  WARNING THIS IS GENERATED CODE.  DO NOT EDIT.

  The only manually maintained code is the code between the //==== (normally where you add your imports)

*/

//====
import ast._
//====


object Mxast {
  
  trait MxRepo {
  
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[Repo,a8.shared.json.ast.JsObj] =
      a8.shared.json.JsonObjectCodecBuilder(generator)
        .addField(_.header)
        .addField(_.organization)
        .addField(_.gradle)
        .addField(_.public)
        .addField(_.modules)
        .build
    
    implicit val catsEq: cats.Eq[Repo] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[Repo,parameters.type] =  {
      val constructors = Constructors[Repo](5, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val header: CaseClassParm[Repo,Option[String]] = CaseClassParm[Repo,Option[String]]("header", _.header, (d,v) => d.copy(header = v), Some(()=> None), 0)
      lazy val organization: CaseClassParm[Repo,String] = CaseClassParm[Repo,String]("organization", _.organization, (d,v) => d.copy(organization = v), None, 1)
      lazy val gradle: CaseClassParm[Repo,Boolean] = CaseClassParm[Repo,Boolean]("gradle", _.gradle, (d,v) => d.copy(gradle = v), Some(()=> false), 2)
      lazy val public: CaseClassParm[Repo,Boolean] = CaseClassParm[Repo,Boolean]("public", _.public, (d,v) => d.copy(public = v), Some(()=> false), 3)
      lazy val modules: CaseClassParm[Repo,Iterable[Module]] = CaseClassParm[Repo,Iterable[Module]]("modules", _.modules, (d,v) => d.copy(modules = v), None, 4)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): Repo = {
        Repo(
          header = values(0).asInstanceOf[Option[String]],
          organization = values(1).asInstanceOf[String],
          gradle = values(2).asInstanceOf[Boolean],
          public = values(3).asInstanceOf[Boolean],
          modules = values(4).asInstanceOf[Iterable[Module]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): Repo = {
        val value =
          Repo(
            header = values.next().asInstanceOf[Option[String]],
            organization = values.next().asInstanceOf[String],
            gradle = values.next().asInstanceOf[Boolean],
            public = values.next().asInstanceOf[Boolean],
            modules = values.next().asInstanceOf[Iterable[Module]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(header: Option[String], organization: String, gradle: Boolean, public: Boolean, modules: Iterable[Module]): Repo =
        Repo(header, organization, gradle, public, modules)
    
    }
    
    
    lazy val typeName = "Repo"
  
  }
  
  
  
  
  trait MxModule {
  
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[Module,a8.shared.json.ast.JsObj] =
      a8.shared.json.JsonObjectCodecBuilder(generator)
        .addField(_.sbtName)
        .addField(_.projectType)
        .addField(_.artifactName)
        .addField(_.directory)
        .addField(_.dependsOn)
        .addField(_.dependencies)
        .addField(_.jvmDependencies)
        .addField(_.jsDependencies)
        .addField(_.extraSettings)
        .build
    
    implicit val catsEq: cats.Eq[Module] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[Module,parameters.type] =  {
      val constructors = Constructors[Module](9, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val sbtName: CaseClassParm[Module,String] = CaseClassParm[Module,String]("sbtName", _.sbtName, (d,v) => d.copy(sbtName = v), None, 0)
      lazy val projectType: CaseClassParm[Module,Option[String]] = CaseClassParm[Module,Option[String]]("projectType", _.projectType, (d,v) => d.copy(projectType = v), None, 1)
      lazy val artifactName: CaseClassParm[Module,Option[String]] = CaseClassParm[Module,Option[String]]("artifactName", _.artifactName, (d,v) => d.copy(artifactName = v), None, 2)
      lazy val directory: CaseClassParm[Module,Option[String]] = CaseClassParm[Module,Option[String]]("directory", _.directory, (d,v) => d.copy(directory = v), None, 3)
      lazy val dependsOn: CaseClassParm[Module,Iterable[String]] = CaseClassParm[Module,Iterable[String]]("dependsOn", _.dependsOn, (d,v) => d.copy(dependsOn = v), Some(()=> Nil), 4)
      lazy val dependencies: CaseClassParm[Module,Option[String]] = CaseClassParm[Module,Option[String]]("dependencies", _.dependencies, (d,v) => d.copy(dependencies = v), None, 5)
      lazy val jvmDependencies: CaseClassParm[Module,Option[String]] = CaseClassParm[Module,Option[String]]("jvmDependencies", _.jvmDependencies, (d,v) => d.copy(jvmDependencies = v), None, 6)
      lazy val jsDependencies: CaseClassParm[Module,Option[String]] = CaseClassParm[Module,Option[String]]("jsDependencies", _.jsDependencies, (d,v) => d.copy(jsDependencies = v), None, 7)
      lazy val extraSettings: CaseClassParm[Module,Option[String]] = CaseClassParm[Module,Option[String]]("extraSettings", _.extraSettings, (d,v) => d.copy(extraSettings = v), None, 8)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): Module = {
        Module(
          sbtName = values(0).asInstanceOf[String],
          projectType = values(1).asInstanceOf[Option[String]],
          artifactName = values(2).asInstanceOf[Option[String]],
          directory = values(3).asInstanceOf[Option[String]],
          dependsOn = values(4).asInstanceOf[Iterable[String]],
          dependencies = values(5).asInstanceOf[Option[String]],
          jvmDependencies = values(6).asInstanceOf[Option[String]],
          jsDependencies = values(7).asInstanceOf[Option[String]],
          extraSettings = values(8).asInstanceOf[Option[String]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): Module = {
        val value =
          Module(
            sbtName = values.next().asInstanceOf[String],
            projectType = values.next().asInstanceOf[Option[String]],
            artifactName = values.next().asInstanceOf[Option[String]],
            directory = values.next().asInstanceOf[Option[String]],
            dependsOn = values.next().asInstanceOf[Iterable[String]],
            dependencies = values.next().asInstanceOf[Option[String]],
            jvmDependencies = values.next().asInstanceOf[Option[String]],
            jsDependencies = values.next().asInstanceOf[Option[String]],
            extraSettings = values.next().asInstanceOf[Option[String]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(sbtName: String, projectType: Option[String], artifactName: Option[String], directory: Option[String], dependsOn: Iterable[String], dependencies: Option[String], jvmDependencies: Option[String], jsDependencies: Option[String], extraSettings: Option[String]): Module =
        Module(sbtName, projectType, artifactName, directory, dependsOn, dependencies, jvmDependencies, jsDependencies, extraSettings)
    
    }
    
    
    lazy val typeName = "Module"
  
  }
  
  
  
  
  trait MxDependency {
  
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[Dependency,a8.shared.json.ast.JsObj] =
      a8.shared.json.JsonObjectCodecBuilder(generator)
        .addField(_.organization)
        .addField(_.scalaArtifactSeparator)
        .addField(_.artifactName)
        .addField(_.version)
        .addField(_.configuration)
        .addField(_.exclusions)
        .build
    
    implicit val catsEq: cats.Eq[Dependency] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[Dependency,parameters.type] =  {
      val constructors = Constructors[Dependency](6, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val organization: CaseClassParm[Dependency,String] = CaseClassParm[Dependency,String]("organization", _.organization, (d,v) => d.copy(organization = v), None, 0)
      lazy val scalaArtifactSeparator: CaseClassParm[Dependency,String] = CaseClassParm[Dependency,String]("scalaArtifactSeparator", _.scalaArtifactSeparator, (d,v) => d.copy(scalaArtifactSeparator = v), None, 1)
      lazy val artifactName: CaseClassParm[Dependency,String] = CaseClassParm[Dependency,String]("artifactName", _.artifactName, (d,v) => d.copy(artifactName = v), None, 2)
      lazy val version: CaseClassParm[Dependency,Identifier] = CaseClassParm[Dependency,Identifier]("version", _.version, (d,v) => d.copy(version = v), None, 3)
      lazy val configuration: CaseClassParm[Dependency,Option[String]] = CaseClassParm[Dependency,Option[String]]("configuration", _.configuration, (d,v) => d.copy(configuration = v), Some(()=> None), 4)
      lazy val exclusions: CaseClassParm[Dependency,Iterable[(String,String)]] = CaseClassParm[Dependency,Iterable[(String,String)]]("exclusions", _.exclusions, (d,v) => d.copy(exclusions = v), Some(()=> Nil), 5)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): Dependency = {
        Dependency(
          organization = values(0).asInstanceOf[String],
          scalaArtifactSeparator = values(1).asInstanceOf[String],
          artifactName = values(2).asInstanceOf[String],
          version = values(3).asInstanceOf[Identifier],
          configuration = values(4).asInstanceOf[Option[String]],
          exclusions = values(5).asInstanceOf[Iterable[(String,String)]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): Dependency = {
        val value =
          Dependency(
            organization = values.next().asInstanceOf[String],
            scalaArtifactSeparator = values.next().asInstanceOf[String],
            artifactName = values.next().asInstanceOf[String],
            version = values.next().asInstanceOf[Identifier],
            configuration = values.next().asInstanceOf[Option[String]],
            exclusions = values.next().asInstanceOf[Iterable[(String,String)]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(organization: String, scalaArtifactSeparator: String, artifactName: String, version: Identifier, configuration: Option[String], exclusions: Iterable[(String,String)]): Dependency =
        Dependency(organization, scalaArtifactSeparator, artifactName, version, configuration, exclusions)
    
    }
    
    
    lazy val typeName = "Dependency"
  
  }
}
