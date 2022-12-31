package io.accur8.neodeploy.plugin


import a8.shared.json.ast.JsDoc
import io.accur8.neodeploy.plugin.PluginManager.SingletonFactory
import io.accur8.neodeploy.plugin.RepositoryPlugins.{RepositoryPlugin, factories}
import io.accur8.neodeploy.resolvedmodel.ResolvedRepository
import a8.shared.SharedImports._

object RepositoryPlugins {

  trait RepositoryPlugin extends Plugin[ResolvedRepository]

  lazy val factories: Vector[PluginManager.Factory[ResolvedRepository, RepositoryPlugin]] =
    Vector(
      SingletonFactory(DnsPlugin),
    )

}


case class RepositoryPlugins(
  repo: ResolvedRepository,
) extends PluginManager[ResolvedRepository, RepositoryPlugin](repo, repo.descriptor.plugins, factories) {
  override def context: String = z"repository ${repo.gitRootDirectory}"
}