package example

import a8.versions.RepositoryOps
import coursier.core.{Module, ModuleName, Organization}

object VersionsDemo extends App {

  val module = Module(Organization("a8"), ModuleName("a8-qubes-dist_2.12"), Map())

  val localVersions = RepositoryOps.localVersions(module)

  val remoteVersions = RepositoryOps.remoteVersions(module)

  toString

}
