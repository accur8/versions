package io.accur8.neodeploy


import a8.shared.json.{JsonCodec, ast}
import a8.shared.json.ast.{JsArr, JsDoc, JsNothing, JsStr, JsVal}
import io.accur8.neodeploy.resolvedmodel.{ResolvedAuthorizedKey, ResolvedPgbackrestClient, ResolvedPgbackrestServer, ResolvedRSnapshotClient, ResolvedRSnapshotServer, ResolvedUser}
import org.typelevel.ci.CIString
import a8.shared.SharedImports._
import a8.shared.app.Logging
import io.accur8.neodeploy.model.{AuthorizedKey, QualifiedUserName}
import zio.Task

object UserPlugin extends Logging {

  object Factory {
    abstract class AbstractFactory[Descriptor : JsonCodec](name0: String) extends Factory {
      lazy val name = CIString(name0)
      def apply(jsd: JsDoc, user: ResolvedUser): Either[String,UserPlugin] = {
        jsd.as[Descriptor] match {
          case Left(re) =>
            Left(s"unable to resolve plugin ${name}${"\n"}    Plugin json: ${jsd.compactJson}${"\n"}    Error message: ${re.prettyMessage}")
          case Right(descriptor) =>
            Right(apply(descriptor, user))
        }
      }

      def apply(descriptor: Descriptor, user: ResolvedUser): UserPlugin

    }
  }

  trait Factory {
    val name: CIString
    def apply(jsd: JsDoc, user: ResolvedUser): Either[String,UserPlugin]
  }

  lazy val factories: Vector[Factory] =
    Vector(
      ResolvedRSnapshotServer,
      ResolvedRSnapshotClient,
      ResolvedPgbackrestClient,
      ResolvedPgbackrestServer,
    )

  case class UserPlugins(jsd: JsDoc, user: ResolvedUser) {

    def descriptorJson =
      JsArr(
        pluginInstances
          .map(_.descriptorJson)
          .toList
      )

    def resolveAuthorizedKeys: Task[Vector[ResolvedAuthorizedKey]] =
      pluginInstances
        .map { plugin =>
          plugin
            .resolveAuthorizedKeys
        }
        .sequence
        .map(_.flatten)

    lazy val pluginInstances: Vector[UserPlugin] = {
      val errors = rawPluginInstances.flatMap(_.left.toOption)
      if ( errors.nonEmpty ) {
        logger.warn(z"plugin errors for user ${user.qualifiedUserName}${"\n"}${errors.mkString("\n").indent("        ")}")
      }
      rawPluginInstances
        .flatMap(_.toOption)
    }

    lazy val rawPluginInstances: Vector[Either[String, UserPlugin]] = {

      def createPlugin(name: String, pluginJsv: JsVal): Either[String,UserPlugin] = {
        val nameCi = CIString(name)
        factories
          .find(_.name == nameCi)
          .map(_.apply(pluginJsv.toDoc, user))
          .getOrElse(Left(s"unable to resolve plugin named ${name} -- ${pluginJsv.compactJson}"))
      }

      def error: Either[String,UserPlugin] =
        Left(s"unable to resolve plugin for ${user.qname} -- ${jsd.compactJson}")

      def impl(pluginJsv: JsDoc): Vector[Either[String,UserPlugin]] = {
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
              .flatMap(v => impl(v.toDoc))
          case JsNothing =>
            Vector.empty
          case _ =>
            Vector(error)
        }
      }

      impl(jsd)

    }

  lazy val resolvedRSnapshotServerOpt =
    pluginInstances
      .collect {
        case rrss: ResolvedRSnapshotServer =>
          rrss
      }
      .headOption

    lazy val resolvedRSnapshotClientOpt =
      pluginInstances
        .collect {
          case rrsc: ResolvedRSnapshotClient =>
            rrsc
        }
        .headOption

    lazy val pgbackrestClientOpt =
      pluginInstances
        .collect {
          case pbc: ResolvedPgbackrestClient =>
            pbc
        }
        .headOption

    lazy val pgbackrestServerOpt =
      pluginInstances
        .collect {
          case pbs: ResolvedPgbackrestServer =>
            pbs
        }
        .headOption

  }

}


trait UserPlugin {

  def name: String
  def descriptorJson: JsVal

  final def resolveAuthorizedKeys: Task[Vector[ResolvedAuthorizedKey]] =
    resolveAuthorizedKeysImpl
      .map(_.map(_.withParent(name)))

  def resolveAuthorizedKeysImpl: Task[Vector[ResolvedAuthorizedKey]] =
    zsucceed(Vector.empty)

}

