package io.accur8.neodeploy.systemstate

/**

  WARNING THIS IS GENERATED CODE.  DO NOT EDIT.

  The only manually maintained code is the code between the //==== (normally where you add your imports)

*/

//====
import io.accur8.neodeploy.Sync.SyncName
import io.accur8.neodeploy.systemstate.SystemStateModel._
//====

import a8.shared.Meta.{CaseClassParm, Generator, Constructors}



object MxSystemStateModel {
  
  trait MxPreviousState {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[PreviousState,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[PreviousState,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[PreviousState,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.resolvedSyncState)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[PreviousState] = zio.prelude.Equal.default
    
    implicit val catsEq: cats.Eq[PreviousState] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[PreviousState,parameters.type] =  {
      val constructors = Constructors[PreviousState](1, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val resolvedSyncState: CaseClassParm[PreviousState,ResolvedState] = CaseClassParm[PreviousState,ResolvedState]("resolvedSyncState", _.resolvedSyncState, (d,v) => d.copy(resolvedSyncState = v), None, 0)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): PreviousState = {
        PreviousState(
          resolvedSyncState = values(0).asInstanceOf[ResolvedState],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): PreviousState = {
        val value =
          PreviousState(
            resolvedSyncState = values.next().asInstanceOf[ResolvedState],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(resolvedSyncState: ResolvedState): PreviousState =
        PreviousState(resolvedSyncState)
    
    }
    
    
    lazy val typeName = "PreviousState"
  
  }
  
  
  
  
  trait MxResolvedState {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[ResolvedState,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[ResolvedState,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[ResolvedState,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.resolvedName)
          .addField(_.syncName)
          .addField(_.value)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[ResolvedState] = zio.prelude.Equal.default
    
    implicit val catsEq: cats.Eq[ResolvedState] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[ResolvedState,parameters.type] =  {
      val constructors = Constructors[ResolvedState](3, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val resolvedName: CaseClassParm[ResolvedState,String] = CaseClassParm[ResolvedState,String]("resolvedName", _.resolvedName, (d,v) => d.copy(resolvedName = v), None, 0)
      lazy val syncName: CaseClassParm[ResolvedState,SyncName] = CaseClassParm[ResolvedState,SyncName]("syncName", _.syncName, (d,v) => d.copy(syncName = v), None, 1)
      lazy val value: CaseClassParm[ResolvedState,SystemState] = CaseClassParm[ResolvedState,SystemState]("value", _.value, (d,v) => d.copy(value = v), None, 2)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): ResolvedState = {
        ResolvedState(
          resolvedName = values(0).asInstanceOf[String],
          syncName = values(1).asInstanceOf[SyncName],
          value = values(2).asInstanceOf[SystemState],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): ResolvedState = {
        val value =
          ResolvedState(
            resolvedName = values.next().asInstanceOf[String],
            syncName = values.next().asInstanceOf[SyncName],
            value = values.next().asInstanceOf[SystemState],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(resolvedName: String, syncName: SyncName, value: SystemState): ResolvedState =
        ResolvedState(resolvedName, syncName, value)
    
    }
    
    
    lazy val typeName = "ResolvedState"
  
  }
  
  
  
  
  trait MxNewState {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[NewState,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[NewState,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[NewState,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.resolvedSyncState)
      )
      .build
    
    implicit val zioEq: zio.prelude.Equal[NewState] = zio.prelude.Equal.default
    
    implicit val catsEq: cats.Eq[NewState] = cats.Eq.fromUniversalEquals
    
    lazy val generator: Generator[NewState,parameters.type] =  {
      val constructors = Constructors[NewState](1, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val resolvedSyncState: CaseClassParm[NewState,ResolvedState] = CaseClassParm[NewState,ResolvedState]("resolvedSyncState", _.resolvedSyncState, (d,v) => d.copy(resolvedSyncState = v), None, 0)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): NewState = {
        NewState(
          resolvedSyncState = values(0).asInstanceOf[ResolvedState],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): NewState = {
        val value =
          NewState(
            resolvedSyncState = values.next().asInstanceOf[ResolvedState],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(resolvedSyncState: ResolvedState): NewState =
        NewState(resolvedSyncState)
    
    }
    
    
    lazy val typeName = "NewState"
  
  }
}
