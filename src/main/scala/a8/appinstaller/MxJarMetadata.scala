package a8.appinstaller

import a8.shared.Meta.{CaseClassParm, Generator, Constructors}

/**

  WARNING THIS IS GENERATED CODE.  DO NOT EDIT.

  The only manually maintained code is the code between the //==== (normally where you add your imports)

*/

//====
import a8.appinstaller.JarMetadata._
//====


object MxJarMetadata {
  
  trait MxExplode {
  
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[Explode,a8.shared.json.ast.JsObj] =
      a8.shared.json.JsonObjectCodecBuilder(generator)
        .addField(_.jar)
        .addField(_.includes)
        .build
    
    implicit val catsEq: cats.Eq[Explode] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[Explode,parameters.type] =  {
      val constructors = Constructors[Explode](2, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val jar: CaseClassParm[Explode,String] = CaseClassParm[Explode,String]("jar", _.jar, (d,v) => d.copy(jar = v), None, 0)
      lazy val includes: CaseClassParm[Explode,Option[String]] = CaseClassParm[Explode,Option[String]]("includes", _.includes, (d,v) => d.copy(includes = v), None, 1)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): Explode = {
        Explode(
          jar = values(0).asInstanceOf[String],
          includes = values(1).asInstanceOf[Option[String]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): Explode = {
        val value =
          Explode(
            jar = values.next().asInstanceOf[String],
            includes = values.next().asInstanceOf[Option[String]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(jar: String, includes: Option[String]): Explode =
        Explode(jar, includes)
    
    }
    
    
    lazy val typeName = "Explode"
  
  }
  
  
  
  
  trait MxSymLink {
  
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[SymLink,a8.shared.json.ast.JsObj] =
      a8.shared.json.JsonObjectCodecBuilder(generator)
        .addField(_.link)
        .addField(_.target)
        .build
    
    implicit val catsEq: cats.Eq[SymLink] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[SymLink,parameters.type] =  {
      val constructors = Constructors[SymLink](2, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val link: CaseClassParm[SymLink,String] = CaseClassParm[SymLink,String]("link", _.link, (d,v) => d.copy(link = v), None, 0)
      lazy val target: CaseClassParm[SymLink,String] = CaseClassParm[SymLink,String]("target", _.target, (d,v) => d.copy(target = v), None, 1)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): SymLink = {
        SymLink(
          link = values(0).asInstanceOf[String],
          target = values(1).asInstanceOf[String],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): SymLink = {
        val value =
          SymLink(
            link = values.next().asInstanceOf[String],
            target = values.next().asInstanceOf[String],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(link: String, target: String): SymLink =
        SymLink(link, target)
    
    }
    
    
    lazy val typeName = "SymLink"
  
  }
  
  
  
  
  trait MxJarMetadata {
  
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[JarMetadata,a8.shared.json.ast.JsObj] =
      a8.shared.json.JsonObjectCodecBuilder(generator)
        .addField(_.explode)
        .addField(_.chmod_exec)
        .addField(_.symlinks)
        .build
    
    implicit val catsEq: cats.Eq[JarMetadata] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[JarMetadata,parameters.type] =  {
      val constructors = Constructors[JarMetadata](3, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val explode: CaseClassParm[JarMetadata,List[JarMetadata.Explode]] = CaseClassParm[JarMetadata,List[JarMetadata.Explode]]("explode", _.explode, (d,v) => d.copy(explode = v), None, 0)
      lazy val chmod_exec: CaseClassParm[JarMetadata,List[String]] = CaseClassParm[JarMetadata,List[String]]("chmod_exec", _.chmod_exec, (d,v) => d.copy(chmod_exec = v), None, 1)
      lazy val symlinks: CaseClassParm[JarMetadata,List[JarMetadata.SymLink]] = CaseClassParm[JarMetadata,List[JarMetadata.SymLink]]("symlinks", _.symlinks, (d,v) => d.copy(symlinks = v), None, 2)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): JarMetadata = {
        JarMetadata(
          explode = values(0).asInstanceOf[List[JarMetadata.Explode]],
          chmod_exec = values(1).asInstanceOf[List[String]],
          symlinks = values(2).asInstanceOf[List[JarMetadata.SymLink]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): JarMetadata = {
        val value =
          JarMetadata(
            explode = values.next().asInstanceOf[List[JarMetadata.Explode]],
            chmod_exec = values.next().asInstanceOf[List[String]],
            symlinks = values.next().asInstanceOf[List[JarMetadata.SymLink]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(explode: List[JarMetadata.Explode], chmod_exec: List[String], symlinks: List[JarMetadata.SymLink]): JarMetadata =
        JarMetadata(explode, chmod_exec, symlinks)
    
    }
    
    
    lazy val typeName = "JarMetadata"
  
  }
}
