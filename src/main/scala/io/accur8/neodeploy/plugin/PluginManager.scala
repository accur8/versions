package io.accur8.neodeploy.plugin

import a8.common.logging.LoggingF
import a8.shared.json.{JsonCodec, ast}
import a8.shared.json.ast.{JsArr, JsDoc, JsNothing, JsStr, JsVal}
import io.accur8.neodeploy.UserPlugin
import io.accur8.neodeploy.plugin.PluginManager.Factory
import io.accur8.neodeploy.resolvedmodel.ResolvedUser
import org.typelevel.ci.CIString
import a8.shared.SharedImports._
import io.accur8.neodeploy.PredefAssist.{given, *}

object PluginManager {

  import a8.Scala3Hacks.*

  object Factory {
    abstract class AbstractFactory[A, B <: Plugin[A], Descriptor: JsonCodec](name0: String) extends Factory[A,B] {

      lazy val name = CIString(name0)

      def apply(jsd: JsDoc, outlet: A): Either[String, B] = {
        jsd.as[Descriptor] match {
          case Left(re) =>
            Left(s"unable to resolve plugin ${name}${"\n"}    Plugin json: ${jsd.compactJson}${"\n"}    Error message: ${re.prettyMessage}")
          case Right(descriptor) =>
            Right(apply(descriptor, outlet))
        }
      }

      def apply(descriptor: Descriptor, plug: A): B

    }
  }

  trait Factory[A, B <: Plugin[A]] {
    lazy val name: CIString
    def apply(jsd: JsDoc, outlet: A): Either[String, B]
  }

  case class SingletonFactory[A, B <: Plugin[A]](singleton: B) extends Factory[A, B] {
    override lazy val name: CIString = CIString(singleton.name)
    override def apply(jsd: JsDoc, outlet: A): Either[String, B] =
      Right(singleton)
  }

}

abstract class PluginManager[A,B <: Plugin[A]](outlet: A, jsd: JsDoc, factories: Vector[Factory[A,B]]) extends LoggingF {

  def context: String

  def descriptorJson =
    JsArr(
      pluginInstances
        .map(_.descriptorJson)
        .toList
    )
  lazy val pluginInstances: Vector[B] = {
    val errors = rawPluginInstances.flatMap(_.left.toOption)
    if (errors.nonEmpty) {
      logger.warn(z"plugin errors for user ${context}${"\n"}${errors.mkString("\n").indent("        ")}")
    }
    rawPluginInstances
      .flatMap(_.toOption)
  }

  lazy val rawPluginInstances: Vector[Either[String, B]] = {

    def createPlugin(name: String, pluginJsv: JsVal): Either[String, B] = {
      val nameCi = CIString(name)
      factories
        .find(_.name == nameCi)
        .map(_.apply(pluginJsv.toRootDoc, outlet))
        .getOrElse(Left(s"unable to resolve plugin named ${name} -- ${pluginJsv.compactJson}"))
    }

    def error: Either[String, B] =
      Left(s"unable to resolve plugin for ${context} -- ${jsd.compactJson}")

    def impl(pluginJsv: JsDoc): Vector[Either[String, B]] = {
      pluginJsv.actualJsVal match {
        case JsStr(name) =>
          Vector(createPlugin(name, JsNothing))
        case jso: ast.JsObj =>
          jso("name").actualJsVal match {
            case JsStr(name) =>
              Vector(createPlugin(name, jso))
            case _ =>
              Vector(error)
          }
        case jsa: ast.JsArr =>
          jsa
            .values
            .toVector
            .flatMap(v => impl(v.toRootDoc))
        case JsNothing =>
          Vector.empty
        case _ =>
          Vector(error)
      }
    }

    impl(jsd)

  }

}
