package a8.appinstaller

/**

  WARNING THIS IS GENERATED CODE.  DO NOT EDIT.

  The only manually maintained code is the code between the //==== (normally where you add your imports)

*/

//====
// noop import so IDE generated imports get put inside the comments block, this can be removed once you have at least one other import
import _root_.scala
import a8.versions.ParsedVersion
import io.accur8.neodeploy.model._
import a8.versions.model.BranchName
//====

import a8.shared.Meta.{CaseClassParm, Generator, Constructors}



object MxAppInstallerConfig {
  
  trait MxAppInstallerConfig {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[AppInstallerConfig,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[AppInstallerConfig,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[AppInstallerConfig,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.organization)
          .addField(_.artifact)
          .addField(_.version)
          .addField(_.branch)
          .addField(_.installDir)
          .addField(_.libDirKind)
          .addField(_.webappExplode)
          .addField(_.backup)
      )
      .build
    
    
    given scala.CanEqual[AppInstallerConfig, AppInstallerConfig] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[AppInstallerConfig,parameters.type] =  {
      val constructors = Constructors[AppInstallerConfig](8, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val organization: CaseClassParm[AppInstallerConfig,Organization] = CaseClassParm[AppInstallerConfig,Organization]("organization", _.organization, (d,v) => d.copy(organization = v), None, 0)
      lazy val artifact: CaseClassParm[AppInstallerConfig,Artifact] = CaseClassParm[AppInstallerConfig,Artifact]("artifact", _.artifact, (d,v) => d.copy(artifact = v), None, 1)
      lazy val version: CaseClassParm[AppInstallerConfig,ParsedVersion] = CaseClassParm[AppInstallerConfig,ParsedVersion]("version", _.version, (d,v) => d.copy(version = v), None, 2)
      lazy val branch: CaseClassParm[AppInstallerConfig,Option[BranchName]] = CaseClassParm[AppInstallerConfig,Option[BranchName]]("branch", _.branch, (d,v) => d.copy(branch = v), None, 3)
      lazy val installDir: CaseClassParm[AppInstallerConfig,Option[String]] = CaseClassParm[AppInstallerConfig,Option[String]]("installDir", _.installDir, (d,v) => d.copy(installDir = v), Some(()=> None), 4)
      lazy val libDirKind: CaseClassParm[AppInstallerConfig,Option[AppInstallerConfig.LibDirKind]] = CaseClassParm[AppInstallerConfig,Option[AppInstallerConfig.LibDirKind]]("libDirKind", _.libDirKind, (d,v) => d.copy(libDirKind = v), Some(()=> None), 5)
      lazy val webappExplode: CaseClassParm[AppInstallerConfig,Option[Boolean]] = CaseClassParm[AppInstallerConfig,Option[Boolean]]("webappExplode", _.webappExplode, (d,v) => d.copy(webappExplode = v), Some(()=> None), 6)
      lazy val backup: CaseClassParm[AppInstallerConfig,Boolean] = CaseClassParm[AppInstallerConfig,Boolean]("backup", _.backup, (d,v) => d.copy(backup = v), Some(()=> true), 7)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): AppInstallerConfig = {
        AppInstallerConfig(
          organization = values(0).asInstanceOf[Organization],
          artifact = values(1).asInstanceOf[Artifact],
          version = values(2).asInstanceOf[ParsedVersion],
          branch = values(3).asInstanceOf[Option[BranchName]],
          installDir = values(4).asInstanceOf[Option[String]],
          libDirKind = values(5).asInstanceOf[Option[AppInstallerConfig.LibDirKind]],
          webappExplode = values(6).asInstanceOf[Option[Boolean]],
          backup = values(7).asInstanceOf[Boolean],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): AppInstallerConfig = {
        val value =
          AppInstallerConfig(
            organization = values.next().asInstanceOf[Organization],
            artifact = values.next().asInstanceOf[Artifact],
            version = values.next().asInstanceOf[ParsedVersion],
            branch = values.next().asInstanceOf[Option[BranchName]],
            installDir = values.next().asInstanceOf[Option[String]],
            libDirKind = values.next().asInstanceOf[Option[AppInstallerConfig.LibDirKind]],
            webappExplode = values.next().asInstanceOf[Option[Boolean]],
            backup = values.next().asInstanceOf[Boolean],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(organization: Organization, artifact: Artifact, version: ParsedVersion, branch: Option[BranchName], installDir: Option[String], libDirKind: Option[AppInstallerConfig.LibDirKind], webappExplode: Option[Boolean], backup: Boolean): AppInstallerConfig =
        AppInstallerConfig(organization, artifact, version, branch, installDir, libDirKind, webappExplode, backup)
    
    }
    
    
    lazy val typeName = "AppInstallerConfig"
  
  }
}
