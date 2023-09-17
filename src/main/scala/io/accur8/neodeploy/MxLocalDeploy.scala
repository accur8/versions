package io.accur8.neodeploy

/**

  WARNING THIS IS GENERATED CODE.  DO NOT EDIT.

  The only manually maintained code is the code between the //==== (normally where you add your imports)

*/

//====
// noop import so IDE generated imports get put inside the comments block, this can be removed once you have at least one other import
import _root_.scala
import LocalDeploy.Config
import io.accur8.neodeploy.model.{GitRootDirectory, ServerName, UserLogin}
//====

import a8.shared.Meta.{CaseClassParm, Generator, Constructors}



object MxLocalDeploy {
  
  trait MxConfig {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[Config,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[Config,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[Config,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.gitRootDirectory)
          .addField(_.serverName)
          .addField(_.userLogin)
      )
      .build
    
    
    given scala.CanEqual[Config, Config] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[Config,parameters.type] =  {
      val constructors = Constructors[Config](3, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val gitRootDirectory: CaseClassParm[Config,GitRootDirectory] = CaseClassParm[Config,GitRootDirectory]("gitRootDirectory", _.gitRootDirectory, (d,v) => d.copy(gitRootDirectory = v), None, 0)
      lazy val serverName: CaseClassParm[Config,ServerName] = CaseClassParm[Config,ServerName]("serverName", _.serverName, (d,v) => d.copy(serverName = v), None, 1)
      lazy val userLogin: CaseClassParm[Config,UserLogin] = CaseClassParm[Config,UserLogin]("userLogin", _.userLogin, (d,v) => d.copy(userLogin = v), Some(()=> UserLogin.thisUser()), 2)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): Config = {
        Config(
          gitRootDirectory = values(0).asInstanceOf[GitRootDirectory],
          serverName = values(1).asInstanceOf[ServerName],
          userLogin = values(2).asInstanceOf[UserLogin],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): Config = {
        val value =
          Config(
            gitRootDirectory = values.next().asInstanceOf[GitRootDirectory],
            serverName = values.next().asInstanceOf[ServerName],
            userLogin = values.next().asInstanceOf[UserLogin],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(gitRootDirectory: GitRootDirectory, serverName: ServerName, userLogin: UserLogin): Config =
        Config(gitRootDirectory, serverName, userLogin)
    
    }
    
    
    lazy val typeName = "Config"
  
  }
}
