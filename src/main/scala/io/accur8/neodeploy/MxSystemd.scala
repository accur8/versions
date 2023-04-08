package io.accur8.neodeploy

/**

  WARNING THIS IS GENERATED CODE.  DO NOT EDIT.

  The only manually maintained code is the code between the //==== (normally where you add your imports)

*/

//====
import io.accur8.neodeploy.Systemd.UnitFile
//====

import a8.shared.Meta.{CaseClassParm, Generator, Constructors}



object MxSystemd {
  
  trait MxUnitFile {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[UnitFile,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[UnitFile,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[UnitFile,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.Type)
          .addField(_.environment)
          .addField(_.workingDirectory)
          .addField(_.execStart)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[UnitFile] = zio.prelude.Equal.default
    
    
    
    
    lazy val generator: Generator[UnitFile,parameters.type] =  {
      val constructors = Constructors[UnitFile](4, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val Type: CaseClassParm[UnitFile,String] = CaseClassParm[UnitFile,String]("Type", _.Type, (d,v) => d.copy(Type = v), Some(()=> "simple"), 0)
      lazy val environment: CaseClassParm[UnitFile,Vector[String]] = CaseClassParm[UnitFile,Vector[String]]("environment", _.environment, (d,v) => d.copy(environment = v), Some(()=> Vector.empty), 1)
      lazy val workingDirectory: CaseClassParm[UnitFile,String] = CaseClassParm[UnitFile,String]("workingDirectory", _.workingDirectory, (d,v) => d.copy(workingDirectory = v), None, 2)
      lazy val execStart: CaseClassParm[UnitFile,String] = CaseClassParm[UnitFile,String]("execStart", _.execStart, (d,v) => d.copy(execStart = v), None, 3)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): UnitFile = {
        UnitFile(
          Type = values(0).asInstanceOf[String],
          environment = values(1).asInstanceOf[Vector[String]],
          workingDirectory = values(2).asInstanceOf[String],
          execStart = values(3).asInstanceOf[String],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): UnitFile = {
        val value =
          UnitFile(
            Type = values.next().asInstanceOf[String],
            environment = values.next().asInstanceOf[Vector[String]],
            workingDirectory = values.next().asInstanceOf[String],
            execStart = values.next().asInstanceOf[String],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(Type: String, environment: Vector[String], workingDirectory: String, execStart: String): UnitFile =
        UnitFile(Type, environment, workingDirectory, execStart)
    
    }
    
    
    lazy val typeName = "UnitFile"
  
  }
}
