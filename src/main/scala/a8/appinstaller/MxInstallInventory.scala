package a8.appinstaller

/**

  WARNING THIS IS GENERATED CODE.  DO NOT EDIT.

  The only manually maintained code is the code between the //==== (normally where you add your imports)

*/

//====
import _root_.scala // noop import so IDE generated imports get put inside the comments block, this can be removed once you have at least one other import
//====

import a8.shared.Meta.{CaseClassParm, Generator, Constructors}



object MxInstallInventory {
  
  trait MxInstallInventory {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[InstallInventory,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[InstallInventory,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[InstallInventory,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.appInstallerConfig)
          .addField(_.classpath)
      )
      .build
    
    
    given scala.CanEqual[InstallInventory, InstallInventory] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[InstallInventory,parameters.type] =  {
      val constructors = Constructors[InstallInventory](2, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val appInstallerConfig: CaseClassParm[InstallInventory,AppInstallerConfig] = CaseClassParm[InstallInventory,AppInstallerConfig]("appInstallerConfig", _.appInstallerConfig, (d,v) => d.copy(appInstallerConfig = v), None, 0)
      lazy val classpath: CaseClassParm[InstallInventory,Iterable[String]] = CaseClassParm[InstallInventory,Iterable[String]]("classpath", _.classpath, (d,v) => d.copy(classpath = v), None, 1)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): InstallInventory = {
        InstallInventory(
          appInstallerConfig = values(0).asInstanceOf[AppInstallerConfig],
          classpath = values(1).asInstanceOf[Iterable[String]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): InstallInventory = {
        val value =
          InstallInventory(
            appInstallerConfig = values.next().asInstanceOf[AppInstallerConfig],
            classpath = values.next().asInstanceOf[Iterable[String]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(appInstallerConfig: AppInstallerConfig, classpath: Iterable[String]): InstallInventory =
        InstallInventory(appInstallerConfig, classpath)
    
    }
    
    
    lazy val typeName = "InstallInventory"
  
  }
}
