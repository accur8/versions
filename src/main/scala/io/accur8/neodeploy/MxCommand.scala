package io.accur8.neodeploy

/**

  WARNING THIS IS GENERATED CODE.  DO NOT EDIT.

  The only manually maintained code is the code between the //==== (normally where you add your imports)

*/

//====
import a8.shared.ZFileSystem.Directory
//====

import a8.shared.Meta.{CaseClassParm, Generator, Constructors}



object MxCommand {
  
  trait MxCommand {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[Command,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[Command,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[Command,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.args)
          .addField(_.workingDirectory)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[Command] = zio.prelude.Equal.default
    
    implicit val catsEq: cats.Eq[Command] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[Command,parameters.type] =  {
      val constructors = Constructors[Command](2, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val args: CaseClassParm[Command,Iterable[String]] = CaseClassParm[Command,Iterable[String]]("args", _.args, (d,v) => d.copy(args = v), None, 0)
      lazy val workingDirectory: CaseClassParm[Command,Option[Directory]] = CaseClassParm[Command,Option[Directory]]("workingDirectory", _.workingDirectory, (d,v) => d.copy(workingDirectory = v), Some(()=> None), 1)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): Command = {
        Command(
          args = values(0).asInstanceOf[Iterable[String]],
          workingDirectory = values(1).asInstanceOf[Option[Directory]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): Command = {
        val value =
          Command(
            args = values.next().asInstanceOf[Iterable[String]],
            workingDirectory = values.next().asInstanceOf[Option[Directory]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(args: Iterable[String], workingDirectory: Option[Directory]): Command =
        Command(args, workingDirectory)
    
    }
    
    
    lazy val typeName = "Command"
  
  }
}
