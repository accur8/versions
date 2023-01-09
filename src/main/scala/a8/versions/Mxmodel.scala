package a8.versions

/**

  WARNING THIS IS GENERATED CODE.  DO NOT EDIT.

  The only manually maintained code is the code between the //==== (normally where you add your imports)

*/

//====
import a8.versions.RepositoryOps.RepoConfigPrefix
import a8.versions.model._
//====

import a8.shared.Meta.{CaseClassParm, Generator, Constructors}



object Mxmodel {
  
  trait MxArtifactResponse {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[ArtifactResponse,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[ArtifactResponse,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[ArtifactResponse,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.url)
          .addField(_.organization)
          .addField(_.module)
          .addField(_.version)
          .addField(_.extension)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[ArtifactResponse] = zio.prelude.Equal.default
    
    implicit val catsEq: cats.Eq[ArtifactResponse] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[ArtifactResponse,parameters.type] =  {
      val constructors = Constructors[ArtifactResponse](5, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val url: CaseClassParm[ArtifactResponse,String] = CaseClassParm[ArtifactResponse,String]("url", _.url, (d,v) => d.copy(url = v), None, 0)
      lazy val organization: CaseClassParm[ArtifactResponse,String] = CaseClassParm[ArtifactResponse,String]("organization", _.organization, (d,v) => d.copy(organization = v), None, 1)
      lazy val module: CaseClassParm[ArtifactResponse,String] = CaseClassParm[ArtifactResponse,String]("module", _.module, (d,v) => d.copy(module = v), None, 2)
      lazy val version: CaseClassParm[ArtifactResponse,String] = CaseClassParm[ArtifactResponse,String]("version", _.version, (d,v) => d.copy(version = v), None, 3)
      lazy val extension: CaseClassParm[ArtifactResponse,String] = CaseClassParm[ArtifactResponse,String]("extension", _.extension, (d,v) => d.copy(extension = v), None, 4)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): ArtifactResponse = {
        ArtifactResponse(
          url = values(0).asInstanceOf[String],
          organization = values(1).asInstanceOf[String],
          module = values(2).asInstanceOf[String],
          version = values(3).asInstanceOf[String],
          extension = values(4).asInstanceOf[String],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): ArtifactResponse = {
        val value =
          ArtifactResponse(
            url = values.next().asInstanceOf[String],
            organization = values.next().asInstanceOf[String],
            module = values.next().asInstanceOf[String],
            version = values.next().asInstanceOf[String],
            extension = values.next().asInstanceOf[String],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(url: String, organization: String, module: String, version: String, extension: String): ArtifactResponse =
        ArtifactResponse(url, organization, module, version, extension)
    
    }
    
    
    lazy val typeName = "ArtifactResponse"
  
  }
  
  
  
  
  trait MxResolutionResponse {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[ResolutionResponse,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[ResolutionResponse,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[ResolutionResponse,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.request)
          .addField(_.version)
          .addField(_.artifacts)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[ResolutionResponse] = zio.prelude.Equal.default
    
    implicit val catsEq: cats.Eq[ResolutionResponse] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[ResolutionResponse,parameters.type] =  {
      val constructors = Constructors[ResolutionResponse](3, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val request: CaseClassParm[ResolutionResponse,ResolutionRequest] = CaseClassParm[ResolutionResponse,ResolutionRequest]("request", _.request, (d,v) => d.copy(request = v), None, 0)
      lazy val version: CaseClassParm[ResolutionResponse,String] = CaseClassParm[ResolutionResponse,String]("version", _.version, (d,v) => d.copy(version = v), None, 1)
      lazy val artifacts: CaseClassParm[ResolutionResponse,Iterable[ArtifactResponse]] = CaseClassParm[ResolutionResponse,Iterable[ArtifactResponse]]("artifacts", _.artifacts, (d,v) => d.copy(artifacts = v), None, 2)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): ResolutionResponse = {
        ResolutionResponse(
          request = values(0).asInstanceOf[ResolutionRequest],
          version = values(1).asInstanceOf[String],
          artifacts = values(2).asInstanceOf[Iterable[ArtifactResponse]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): ResolutionResponse = {
        val value =
          ResolutionResponse(
            request = values.next().asInstanceOf[ResolutionRequest],
            version = values.next().asInstanceOf[String],
            artifacts = values.next().asInstanceOf[Iterable[ArtifactResponse]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(request: ResolutionRequest, version: String, artifacts: Iterable[ArtifactResponse]): ResolutionResponse =
        ResolutionResponse(request, version, artifacts)
    
    }
    
    
    lazy val typeName = "ResolutionResponse"
  
  }
  
  
  
  
  trait MxResolutionRequest {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[ResolutionRequest,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[ResolutionRequest,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[ResolutionRequest,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.repoPrefix)
          .addField(_.organization)
          .addField(_.artifact)
          .addField(_.version)
          .addField(_.branch)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[ResolutionRequest] = zio.prelude.Equal.default
    
    implicit val catsEq: cats.Eq[ResolutionRequest] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[ResolutionRequest,parameters.type] =  {
      val constructors = Constructors[ResolutionRequest](5, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val repoPrefix: CaseClassParm[ResolutionRequest,RepoConfigPrefix] = CaseClassParm[ResolutionRequest,RepoConfigPrefix]("repoPrefix", _.repoPrefix, (d,v) => d.copy(repoPrefix = v), Some(()=> RepoConfigPrefix.default), 0)
      lazy val organization: CaseClassParm[ResolutionRequest,String] = CaseClassParm[ResolutionRequest,String]("organization", _.organization, (d,v) => d.copy(organization = v), None, 1)
      lazy val artifact: CaseClassParm[ResolutionRequest,String] = CaseClassParm[ResolutionRequest,String]("artifact", _.artifact, (d,v) => d.copy(artifact = v), None, 2)
      lazy val version: CaseClassParm[ResolutionRequest,String] = CaseClassParm[ResolutionRequest,String]("version", _.version, (d,v) => d.copy(version = v), None, 3)
      lazy val branch: CaseClassParm[ResolutionRequest,Option[BranchName]] = CaseClassParm[ResolutionRequest,Option[BranchName]]("branch", _.branch, (d,v) => d.copy(branch = v), None, 4)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): ResolutionRequest = {
        ResolutionRequest(
          repoPrefix = values(0).asInstanceOf[RepoConfigPrefix],
          organization = values(1).asInstanceOf[String],
          artifact = values(2).asInstanceOf[String],
          version = values(3).asInstanceOf[String],
          branch = values(4).asInstanceOf[Option[BranchName]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): ResolutionRequest = {
        val value =
          ResolutionRequest(
            repoPrefix = values.next().asInstanceOf[RepoConfigPrefix],
            organization = values.next().asInstanceOf[String],
            artifact = values.next().asInstanceOf[String],
            version = values.next().asInstanceOf[String],
            branch = values.next().asInstanceOf[Option[BranchName]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(repoPrefix: RepoConfigPrefix, organization: String, artifact: String, version: String, branch: Option[BranchName]): ResolutionRequest =
        ResolutionRequest(repoPrefix, organization, artifact, version, branch)
    
    }
    
    
    lazy val typeName = "ResolutionRequest"
  
  }
}
