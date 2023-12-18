package io.accur8.neodeploy


import SharedImports.{*, given}
import io.accur8.neodeploy.Deployable.AppDeployable
import io.accur8.neodeploy.model.SupervisorDescriptor
import io.accur8.neodeploy.resolvedmodel.ResolvedRepository

object Setup {

  def resolveSetupArgs(deployables: ResolvedDeployables, resolvedRepository: ResolvedRepository): ResolvedDeployables = {
    val values =
      deployables
        .asIterable
        .flatMap {
          case ad: AppDeployable =>
            resolveRawSetupArgs(ad)
          case d =>
            Iterable(d.originalArg)
        }
        .map(DeployArgParser.parse(_, resolvedRepository))

    ResolvedDeployables(values)

  }

  private def resolveRawSetupArgs(appDeploy: AppDeployable): Iterable[String] = {
    val descriptor: model.ApplicationDescriptor = appDeploy.resolvedApp.descriptor

    val caddy: Iterable[String] =
      (descriptor.caddyConfig, descriptor.listenPort, descriptor.resolvedDomainNames) match {
        case (None, None, Seq()) =>
          None
        case _ =>

          val dns =
            descriptor
              .resolvedDomainNames
              .map(_.topLevelDomain)
              .distinct
              .map { d => z"dns" }

          val caddy =
            Iterable(
              z"${appDeploy.resolvedApp.server.name}:caddy",
            )

          dns ++ caddy

      }

    val supervisor: Option[String] =
      descriptor.launcher match {
        case sd: SupervisorDescriptor =>
          Some(z"${appDeploy.resolvedApp.server.name}:supervisor")
        case _ =>
          None
      }

    val database: Option[String] =
      descriptor.setup.database match {
        case Some(dbs) =>
          Some(z"${descriptor.resolvedDomainNames.head}:database")
        case None =>
          None
      }

    caddy ++ supervisor ++ database ++ Some(z"${descriptor.resolvedDomainNames.head}")

  }


}
