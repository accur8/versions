package io.accur8.neodeploy.systemstate

/**

  WARNING THIS IS GENERATED CODE.  DO NOT EDIT.

  The only manually maintained code is the code between the //==== (normally where you add your imports)

*/

//====
import io.accur8.neodeploy.DeployId
import io.accur8.neodeploy.systemstate.SystemStateModel.*
import io.accur8.neodeploy.VFileSystem
//====

import a8.shared.Meta.{CaseClassParm, Generator, Constructors}



object MxSystemStateModel {
  
  trait MxStateKey {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[StateKey,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[StateKey,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[StateKey,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.kind)
          .addField(_.value)
      )
      .build
    
    
    given scala.CanEqual[StateKey, StateKey] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[StateKey,parameters.type] =  {
      val constructors = Constructors[StateKey](2, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val kind: CaseClassParm[StateKey,String] = CaseClassParm[StateKey,String]("kind", _.kind, (d,v) => d.copy(kind = v), None, 0)
      lazy val value: CaseClassParm[StateKey,String] = CaseClassParm[StateKey,String]("value", _.value, (d,v) => d.copy(value = v), None, 1)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): StateKey = {
        StateKey(
          kind = values(0).asInstanceOf[String],
          value = values(1).asInstanceOf[String],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): StateKey = {
        val value =
          StateKey(
            kind = values.next().asInstanceOf[String],
            value = values.next().asInstanceOf[String],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(kind: String, value: String): StateKey =
        StateKey(kind, value)
    
    }
    
    
    lazy val typeName = "StateKey"
  
  }
  
  
  
  
  trait MxPreviousState {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[PreviousState,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[PreviousState,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[PreviousState,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.resolvedSyncState)
      )
      .build
    
    
    given scala.CanEqual[PreviousState, PreviousState] = scala.CanEqual.derived
    
    
    
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
          .addField(_.deployId)
          .addField(_.value)
      )
      .build
    
    
    given scala.CanEqual[ResolvedState, ResolvedState] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[ResolvedState,parameters.type] =  {
      val constructors = Constructors[ResolvedState](2, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val deployId: CaseClassParm[ResolvedState,DeployId] = CaseClassParm[ResolvedState,DeployId]("deployId", _.deployId, (d,v) => d.copy(deployId = v), None, 0)
      lazy val value: CaseClassParm[ResolvedState,SystemState] = CaseClassParm[ResolvedState,SystemState]("value", _.value, (d,v) => d.copy(value = v), None, 1)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): ResolvedState = {
        ResolvedState(
          deployId = values(0).asInstanceOf[DeployId],
          value = values(1).asInstanceOf[SystemState],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): ResolvedState = {
        val value =
          ResolvedState(
            deployId = values.next().asInstanceOf[DeployId],
            value = values.next().asInstanceOf[SystemState],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(deployId: DeployId, value: SystemState): ResolvedState =
        ResolvedState(deployId, value)
    
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
    
    
    given scala.CanEqual[NewState, NewState] = scala.CanEqual.derived
    
    
    
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
  
  
  
  
  trait MxCommand {
  
    protected def jsonCodecBuilder(builder: a8.shared.json.JsonObjectCodecBuilder[Command,parameters.type]): a8.shared.json.JsonObjectCodecBuilder[Command,parameters.type] = builder
    
    implicit lazy val jsonCodec: a8.shared.json.JsonTypedCodec[Command,a8.shared.json.ast.JsObj] =
      jsonCodecBuilder(
        a8.shared.json.JsonObjectCodecBuilder(generator)
          .addField(_.args)
          .addField(_.workingDirectory)
      )
      .build
    
    
    given scala.CanEqual[Command, Command] = scala.CanEqual.derived
    
    
    
    lazy val generator: Generator[Command,parameters.type] =  {
      val constructors = Constructors[Command](2, unsafe.iterRawConstruct)
      Generator(constructors, parameters)
    }
    
    object parameters {
      lazy val args: CaseClassParm[Command,Iterable[String]] = CaseClassParm[Command,Iterable[String]]("args", _.args, (d,v) => d.copy(args = v), None, 0)
      lazy val workingDirectory: CaseClassParm[Command,Option[VFileSystem.Directory]] = CaseClassParm[Command,Option[VFileSystem.Directory]]("workingDirectory", _.workingDirectory, (d,v) => d.copy(workingDirectory = v), Some(()=> None), 1)
    }
    
    
    object unsafe {
    
      def rawConstruct(values: IndexedSeq[Any]): Command = {
        Command(
          args = values(0).asInstanceOf[Iterable[String]],
          workingDirectory = values(1).asInstanceOf[Option[VFileSystem.Directory]],
        )
      }
      def iterRawConstruct(values: Iterator[Any]): Command = {
        val value =
          Command(
            args = values.next().asInstanceOf[Iterable[String]],
            workingDirectory = values.next().asInstanceOf[Option[VFileSystem.Directory]],
          )
        if ( values.hasNext )
           sys.error("")
        value
      }
      def typedConstruct(args: Iterable[String], workingDirectory: Option[VFileSystem.Directory]): Command =
        Command(args, workingDirectory)
    
    }
    
    
    lazy val typeName = "Command"
  
  }
}
