package a8.versions

/**

  WARNING THIS IS GENERATED CODE.  DO NOT EDIT.

  The only manually maintained code is the code between the //==== (normally where you add your imports)

*/

//====
// noop import so IDE generated imports get put inside the comments block, this can be removed once you have at least one other import
import _root_.scala
import a8.versions.GenerateJavaLauncherDotNix._
import a8.versions.RepositoryOps.RepoConfigPrefix
import a8.versions.model.BranchName
import io.accur8.neodeploy.model._
//====

import a8.shared.Meta.{CaseClassParm, Generator, Constructors}



object MxGenerateJavaLauncherDotNix {
  
  trait MxParms {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[Parms,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[Parms,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[Parms,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.name)
          .addField(_.mainClass)
          .addField(_.jvmArgs)
          .addField(_.args)
          .addField(_.repo)
          .addField(_.organization)
          .addField(_.artifact)
          .addField(_.version)
          .addField(_.branch)
          .addField(_.webappExplode)
          .addField(_.javaVersion)
          .addField(_.dependencyDownloader)
      )
      .build
    
    
    given scala.CanEqual[Parms, Parms] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[Parms,parameters.type] =  {
      val constructors = Constructors[Parms](12, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val name: CaseClassParm[Parms,String] = CaseClassParm[Parms,String]("name", _.name, (d,v) => d.copy(name = v), None, 0)
      lazy val mainClass: CaseClassParm[Parms,String] = CaseClassParm[Parms,String]("mainClass", _.mainClass, (d,v) => d.copy(mainClass = v), None, 1)
      lazy val jvmArgs: CaseClassParm[Parms,List[String]] = CaseClassParm[Parms,List[String]]("jvmArgs", _.jvmArgs, (d,v) => d.copy(jvmArgs = v), Some(()=> Nil), 2)
      lazy val args: CaseClassParm[Parms,List[String]] = CaseClassParm[Parms,List[String]]("args", _.args, (d,v) => d.copy(args = v), Some(()=> Nil), 3)
      lazy val repo: CaseClassParm[Parms,RepoConfigPrefix] = CaseClassParm[Parms,RepoConfigPrefix]("repo", _.repo, (d,v) => d.copy(repo = v), Some(()=> RepoConfigPrefix.default), 4)
      lazy val organization: CaseClassParm[Parms,Organization] = CaseClassParm[Parms,Organization]("organization", _.organization, (d,v) => d.copy(organization = v), None, 5)
      lazy val artifact: CaseClassParm[Parms,Artifact] = CaseClassParm[Parms,Artifact]("artifact", _.artifact, (d,v) => d.copy(artifact = v), None, 6)
      lazy val version: CaseClassParm[Parms,Option[Version]] = CaseClassParm[Parms,Option[Version]]("version", _.version, (d,v) => d.copy(version = v), Some(()=> None), 7)
      lazy val branch: CaseClassParm[Parms,Option[BranchName]] = CaseClassParm[Parms,Option[BranchName]]("branch", _.branch, (d,v) => d.copy(branch = v), Some(()=> None), 8)
      lazy val webappExplode: CaseClassParm[Parms,Option[Boolean]] = CaseClassParm[Parms,Option[Boolean]]("webappExplode", _.webappExplode, (d,v) => d.copy(webappExplode = v), Some(()=> None), 9)
      lazy val javaVersion: CaseClassParm[Parms,Option[String]] = CaseClassParm[Parms,Option[String]]("javaVersion", _.javaVersion, (d,v) => d.copy(javaVersion = v), Some(()=> None), 10)
      lazy val dependencyDownloader: CaseClassParm[Parms,Option[String]] = CaseClassParm[Parms,Option[String]]("dependencyDownloader", _.dependencyDownloader, (d,v) => d.copy(dependencyDownloader = v), Some(()=> None), 11)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): Parms = {
        Parms(
          name = values(0).asInstanceOf[String],
          mainClass = values(1).asInstanceOf[String],
          jvmArgs = values(2).asInstanceOf[List[String]],
          args = values(3).asInstanceOf[List[String]],
          repo = values(4).asInstanceOf[RepoConfigPrefix],
          organization = values(5).asInstanceOf[Organization],
          artifact = values(6).asInstanceOf[Artifact],
          version = values(7).asInstanceOf[Option[Version]],
          branch = values(8).asInstanceOf[Option[BranchName]],
          webappExplode = values(9).asInstanceOf[Option[Boolean]],
          javaVersion = values(10).asInstanceOf[Option[String]],
          dependencyDownloader = values(11).asInstanceOf[Option[String]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): Parms = {
        val value =
          Parms(
            name = values.next().asInstanceOf[String],
            mainClass = values.next().asInstanceOf[String],
            jvmArgs = values.next().asInstanceOf[List[String]],
            args = values.next().asInstanceOf[List[String]],
            repo = values.next().asInstanceOf[RepoConfigPrefix],
            organization = values.next().asInstanceOf[Organization],
            artifact = values.next().asInstanceOf[Artifact],
            version = values.next().asInstanceOf[Option[Version]],
            branch = values.next().asInstanceOf[Option[BranchName]],
            webappExplode = values.next().asInstanceOf[Option[Boolean]],
            javaVersion = values.next().asInstanceOf[Option[String]],
            dependencyDownloader = values.next().asInstanceOf[Option[String]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(name: String, mainClass: String, jvmArgs: List[String], args: List[String], repo: RepoConfigPrefix, organization: Organization, artifact: Artifact, version: Option[Version], branch: Option[BranchName], webappExplode: Option[Boolean], javaVersion: Option[String], dependencyDownloader: Option[String]): Parms =
        Parms(name, mainClass, jvmArgs, args, repo, organization, artifact, version, branch, webappExplode, javaVersion, dependencyDownloader)
    
    }
    
    
    lazy val typeName = "Parms"
  
  }
  
  
  
  
  trait MxFileContents {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[FileContents,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[FileContents,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[FileContents,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.filename)
          .addField(_.contents)
      )
      .build
    
    
    given scala.CanEqual[FileContents, FileContents] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[FileContents,parameters.type] =  {
      val constructors = Constructors[FileContents](2, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val filename: CaseClassParm[FileContents,String] = CaseClassParm[FileContents,String]("filename", _.filename, (d,v) => d.copy(filename = v), None, 0)
      lazy val contents: CaseClassParm[FileContents,String] = CaseClassParm[FileContents,String]("contents", _.contents, (d,v) => d.copy(contents = v), None, 1)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): FileContents = {
        FileContents(
          filename = values(0).asInstanceOf[String],
          contents = values(1).asInstanceOf[String],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): FileContents = {
        val value =
          FileContents(
            filename = values.next().asInstanceOf[String],
            contents = values.next().asInstanceOf[String],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(filename: String, contents: String): FileContents =
        FileContents(filename, contents)
    
    }
    
    
    lazy val typeName = "FileContents"
  
  }
  
  
  
  
  trait MxBuildDescription {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[BuildDescription,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[BuildDescription,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[BuildDescription,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.files)
          .addField(_.resolvedVersion)
      )
      .build
    
    
    given scala.CanEqual[BuildDescription, BuildDescription] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[BuildDescription,parameters.type] =  {
      val constructors = Constructors[BuildDescription](2, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val files: CaseClassParm[BuildDescription,Iterable[FileContents]] = CaseClassParm[BuildDescription,Iterable[FileContents]]("files", _.files, (d,v) => d.copy(files = v), None, 0)
      lazy val resolvedVersion: CaseClassParm[BuildDescription,io.accur8.neodeploy.model.Version] = CaseClassParm[BuildDescription,io.accur8.neodeploy.model.Version]("resolvedVersion", _.resolvedVersion, (d,v) => d.copy(resolvedVersion = v), None, 1)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): BuildDescription = {
        BuildDescription(
          files = values(0).asInstanceOf[Iterable[FileContents]],
          resolvedVersion = values(1).asInstanceOf[io.accur8.neodeploy.model.Version],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): BuildDescription = {
        val value =
          BuildDescription(
            files = values.next().asInstanceOf[Iterable[FileContents]],
            resolvedVersion = values.next().asInstanceOf[io.accur8.neodeploy.model.Version],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(files: Iterable[FileContents], resolvedVersion: io.accur8.neodeploy.model.Version): BuildDescription =
        BuildDescription(files, resolvedVersion)
    
    }
    
    
    lazy val typeName = "BuildDescription"
  
  }
}
