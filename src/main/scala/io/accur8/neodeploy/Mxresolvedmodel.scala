package io.accur8.neodeploy

/**

  WARNING THIS IS GENERATED CODE.  DO NOT EDIT.

  The only manually maintained code is the code between the //==== (normally where you add your imports)

*/

//====
import _root_.scala
import a8.shared.ZFileSystem.Directory
import io.accur8.neodeploy.model.{ApplicationDescriptor, ServerName, UserLogin}
import io.accur8.neodeploy.resolvedmodel.ResolvedApp.LoadedApplicationDescriptor
//====

import a8.shared.Meta.{CaseClassParm, Generator, Constructors}



object Mxresolvedmodel {
  
  trait MxLoadedApplicationDescriptor {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[LoadedApplicationDescriptor,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[LoadedApplicationDescriptor,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[LoadedApplicationDescriptor,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.appConfigDir)
          .addField(_.serverName)
          .addField(_.userLogin)
          .addField(_.descriptor)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[LoadedApplicationDescriptor] = zio.prelude.Equal.default
    
    implicit val catsEq: cats.Eq[LoadedApplicationDescriptor] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[LoadedApplicationDescriptor,parameters.type] =  {
      val constructors = Constructors[LoadedApplicationDescriptor](4, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val appConfigDir: CaseClassParm[LoadedApplicationDescriptor,Directory] = CaseClassParm[LoadedApplicationDescriptor,Directory]("appConfigDir", _.appConfigDir, (d,v) => d.copy(appConfigDir = v), None, 0)
      lazy val serverName: CaseClassParm[LoadedApplicationDescriptor,ServerName] = CaseClassParm[LoadedApplicationDescriptor,ServerName]("serverName", _.serverName, (d,v) => d.copy(serverName = v), None, 1)
      lazy val userLogin: CaseClassParm[LoadedApplicationDescriptor,UserLogin] = CaseClassParm[LoadedApplicationDescriptor,UserLogin]("userLogin", _.userLogin, (d,v) => d.copy(userLogin = v), None, 2)
      lazy val descriptor: CaseClassParm[LoadedApplicationDescriptor,ApplicationDescriptor] = CaseClassParm[LoadedApplicationDescriptor,ApplicationDescriptor]("descriptor", _.descriptor, (d,v) => d.copy(descriptor = v), None, 3)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): LoadedApplicationDescriptor = {
        LoadedApplicationDescriptor(
          appConfigDir = values(0).asInstanceOf[Directory],
          serverName = values(1).asInstanceOf[ServerName],
          userLogin = values(2).asInstanceOf[UserLogin],
          descriptor = values(3).asInstanceOf[ApplicationDescriptor],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): LoadedApplicationDescriptor = {
        val value =
          LoadedApplicationDescriptor(
            appConfigDir = values.next().asInstanceOf[Directory],
            serverName = values.next().asInstanceOf[ServerName],
            userLogin = values.next().asInstanceOf[UserLogin],
            descriptor = values.next().asInstanceOf[ApplicationDescriptor],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(appConfigDir: Directory, serverName: ServerName, userLogin: UserLogin, descriptor: ApplicationDescriptor): LoadedApplicationDescriptor =
        LoadedApplicationDescriptor(appConfigDir, serverName, userLogin, descriptor)
    
    }
    
    
    lazy val typeName = "LoadedApplicationDescriptor"
  
  }
}
