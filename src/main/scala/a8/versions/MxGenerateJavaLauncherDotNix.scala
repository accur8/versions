package a8.versions

/**

  WARNING THIS IS GENERATED CODE.  DO NOT EDIT.

  The only manually maintained code is the code between the //==== (normally where you add your imports)

*/

//====
// noop import so IDE generated imports get put inside the comments block, this can be removed once you have at least one other import
import _root_.scala
import a8.versions.GenerateJavaLauncherDotNix.Parms
import a8.versions.RepositoryOps.RepoConfigPrefix
import a8.versions.model.BranchName
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
    
    implicit val zioEq: zio.prelude.Equal[Parms] = zio.prelude.Equal.default
    
    
    
    
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
      lazy val organization: CaseClassParm[Parms,String] = CaseClassParm[Parms,String]("organization", _.organization, (d,v) => d.copy(organization = v), None, 5)
      lazy val artifact: CaseClassParm[Parms,String] = CaseClassParm[Parms,String]("artifact", _.artifact, (d,v) => d.copy(artifact = v), None, 6)
      lazy val version: CaseClassParm[Parms,Option[String]] = CaseClassParm[Parms,Option[String]]("version", _.version, (d,v) => d.copy(version = v), Some(()=> None), 7)
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
          organization = values(5).asInstanceOf[String],
          artifact = values(6).asInstanceOf[String],
          version = values(7).asInstanceOf[Option[String]],
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
            organization = values.next().asInstanceOf[String],
            artifact = values.next().asInstanceOf[String],
            version = values.next().asInstanceOf[Option[String]],
            branch = values.next().asInstanceOf[Option[BranchName]],
            webappExplode = values.next().asInstanceOf[Option[Boolean]],
            javaVersion = values.next().asInstanceOf[Option[String]],
            dependencyDownloader = values.next().asInstanceOf[Option[String]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(name: String, mainClass: String, jvmArgs: List[String], args: List[String], repo: RepoConfigPrefix, organization: String, artifact: String, version: Option[String], branch: Option[BranchName], webappExplode: Option[Boolean], javaVersion: Option[String], dependencyDownloader: Option[String]): Parms =
        Parms(name, mainClass, jvmArgs, args, repo, organization, artifact, version, branch, webappExplode, javaVersion, dependencyDownloader)
    
    }
    
    
    lazy val typeName = "Parms"
  
  }
}
