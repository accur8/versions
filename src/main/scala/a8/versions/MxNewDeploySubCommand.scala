package a8.versions

/**

  WARNING THIS IS GENERATED CODE.  DO NOT EDIT.

  The only manually maintained code is the code between the //==== (normally where you add your imports)

*/

//====
// noop import so IDE generated imports get put inside the comments block, this can be removed once you have at least one other import
import _root_.scala
import NewDeploySubCommand.InstallDescriptor
//====

import a8.shared.Meta.{CaseClassParm, Generator, Constructors}



object MxNewDeploySubCommand {
  
  trait MxInstallDescriptor {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[InstallDescriptor,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[InstallDescriptor,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[InstallDescriptor,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.name)
          .addField(_.organization)
          .addField(_.artifact)
          .addField(_.version)
          .addField(_.installDir)
          .addField(_.webappExplode)
          .addField(_.mainClass)
          .addField(_.branch)
          .addField(_.repo)
          .addField(_.javaRuntimeVersion)
          .addField(_.backupDir)
      )
      .build
    
    
    given scala.CanEqual[InstallDescriptor, InstallDescriptor] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[InstallDescriptor,parameters.type] =  {
      val constructors = Constructors[InstallDescriptor](11, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val name: CaseClassParm[InstallDescriptor,String] = CaseClassParm[InstallDescriptor,String]("name", _.name, (d,v) => d.copy(name = v), None, 0)
      lazy val organization: CaseClassParm[InstallDescriptor,String] = CaseClassParm[InstallDescriptor,String]("organization", _.organization, (d,v) => d.copy(organization = v), None, 1)
      lazy val artifact: CaseClassParm[InstallDescriptor,String] = CaseClassParm[InstallDescriptor,String]("artifact", _.artifact, (d,v) => d.copy(artifact = v), None, 2)
      lazy val version: CaseClassParm[InstallDescriptor,String] = CaseClassParm[InstallDescriptor,String]("version", _.version, (d,v) => d.copy(version = v), None, 3)
      lazy val installDir: CaseClassParm[InstallDescriptor,String] = CaseClassParm[InstallDescriptor,String]("installDir", _.installDir, (d,v) => d.copy(installDir = v), None, 4)
      lazy val webappExplode: CaseClassParm[InstallDescriptor,Boolean] = CaseClassParm[InstallDescriptor,Boolean]("webappExplode", _.webappExplode, (d,v) => d.copy(webappExplode = v), None, 5)
      lazy val mainClass: CaseClassParm[InstallDescriptor,String] = CaseClassParm[InstallDescriptor,String]("mainClass", _.mainClass, (d,v) => d.copy(mainClass = v), None, 6)
      lazy val branch: CaseClassParm[InstallDescriptor,String] = CaseClassParm[InstallDescriptor,String]("branch", _.branch, (d,v) => d.copy(branch = v), None, 7)
      lazy val repo: CaseClassParm[InstallDescriptor,String] = CaseClassParm[InstallDescriptor,String]("repo", _.repo, (d,v) => d.copy(repo = v), None, 8)
      lazy val javaRuntimeVersion: CaseClassParm[InstallDescriptor,String] = CaseClassParm[InstallDescriptor,String]("javaRuntimeVersion", _.javaRuntimeVersion, (d,v) => d.copy(javaRuntimeVersion = v), None, 9)
      lazy val backupDir: CaseClassParm[InstallDescriptor,String] = CaseClassParm[InstallDescriptor,String]("backupDir", _.backupDir, (d,v) => d.copy(backupDir = v), None, 10)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): InstallDescriptor = {
        InstallDescriptor(
          name = values(0).asInstanceOf[String],
          organization = values(1).asInstanceOf[String],
          artifact = values(2).asInstanceOf[String],
          version = values(3).asInstanceOf[String],
          installDir = values(4).asInstanceOf[String],
          webappExplode = values(5).asInstanceOf[Boolean],
          mainClass = values(6).asInstanceOf[String],
          branch = values(7).asInstanceOf[String],
          repo = values(8).asInstanceOf[String],
          javaRuntimeVersion = values(9).asInstanceOf[String],
          backupDir = values(10).asInstanceOf[String],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): InstallDescriptor = {
        val value =
          InstallDescriptor(
            name = values.next().asInstanceOf[String],
            organization = values.next().asInstanceOf[String],
            artifact = values.next().asInstanceOf[String],
            version = values.next().asInstanceOf[String],
            installDir = values.next().asInstanceOf[String],
            webappExplode = values.next().asInstanceOf[Boolean],
            mainClass = values.next().asInstanceOf[String],
            branch = values.next().asInstanceOf[String],
            repo = values.next().asInstanceOf[String],
            javaRuntimeVersion = values.next().asInstanceOf[String],
            backupDir = values.next().asInstanceOf[String],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(name: String, organization: String, artifact: String, version: String, installDir: String, webappExplode: Boolean, mainClass: String, branch: String, repo: String, javaRuntimeVersion: String, backupDir: String): InstallDescriptor =
        InstallDescriptor(name, organization, artifact, version, installDir, webappExplode, mainClass, branch, repo, javaRuntimeVersion, backupDir)
    
    }
    
    
    lazy val typeName = "InstallDescriptor"
  
  }
}
