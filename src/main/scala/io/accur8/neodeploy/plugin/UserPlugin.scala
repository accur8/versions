package io.accur8.neodeploy


import a8.shared.json.{JsonCodec, ast}
import a8.shared.json.ast.{JsArr, JsDoc, JsNothing, JsStr, JsVal}
import io.accur8.neodeploy.resolvedmodel.{ResolvedAuthorizedKey, ResolvedUser}
import org.typelevel.ci.CIString
import SharedImports._
import a8.common.logging.Logging
import io.accur8.neodeploy.model.{AuthorizedKey, DomainName, ListenPort, QualifiedUserName}
import io.accur8.neodeploy.plugin.PluginManager.{Factory, SingletonFactory}
import io.accur8.neodeploy.plugin.{CaddyServerPlugin, PgbackrestClientPlugin, PgbackrestServerPlugin, Plugin, PluginManager, RSnapshotClientPlugin, RSnapshotServerPlugin}
import io.accur8.neodeploy.systemstate.SystemState
import io.accur8.neodeploy.systemstate.SystemStateModel.M

object UserPlugin extends Logging {

  abstract class Factory[Descriptor: JsonCodec](name0: String) extends PluginManager.Factory.AbstractFactory[ResolvedUser, UserPlugin, Descriptor](name0)

  lazy val factories: Vector[PluginManager.Factory[ResolvedUser, UserPlugin]] =
    Vector(
      RSnapshotServerPlugin,
      RSnapshotClientPlugin,
      PgbackrestClientPlugin,
      PgbackrestServerPlugin,
      SingletonFactory(CaddyServerPlugin),
    )

  case class UserPlugins(user: ResolvedUser) extends PluginManager[ResolvedUser, UserPlugin](user, user.descriptor.plugins, factories) {

    override def context: String = user.qname

    def resolveAuthorizedKeys: N[Vector[ResolvedAuthorizedKey]] =
      pluginInstances
        .map { plugin =>
          plugin
            .resolveAuthorizedKeys
        }
        .sequence
        .map(_.flatten)

    lazy val resolvedRSnapshotServerOpt =
      pluginInstances
        .collect {
          case rssp: RSnapshotServerPlugin =>
            rssp
        }
        .headOption

    lazy val resolvedRSnapshotClientOpt =
      pluginInstances
        .collect {
          case rscp: RSnapshotClientPlugin =>
            rscp
        }
        .headOption

    lazy val pgbackrestClientOpt =
      pluginInstances
        .collect {
          case pbc: PgbackrestClientPlugin =>
            pbc
        }
        .headOption

    lazy val pgbackrestServerOpt =
      pluginInstances
        .collect {
          case pbs: PgbackrestServerPlugin =>
            pbs
        }
        .headOption

  }

}


trait UserPlugin extends Plugin[ResolvedUser] {

  final def resolveAuthorizedKeys: N[Vector[ResolvedAuthorizedKey]] =
    resolveAuthorizedKeysImpl
      .map(_.map(_.withParent(name)))

  def resolveAuthorizedKeysImpl: N[Vector[ResolvedAuthorizedKey]] =
    zsucceed(Vector.empty)

}

