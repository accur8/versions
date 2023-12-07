package io.accur8.neodeploy


import SharedImports.{*, given}
import io.accur8.neodeploy.model.SupervisorDescriptor

object Setup {

  def resolveSetupArgs(deployArgs: ResolvedDeployArgs): RawDeployArgs = {

    val resolvedArgs: List[String] =
      deployArgs
        .args
        .collect { case da: AppDeploy => da }
        .flatMap { appDeploy =>
          resolveRawSetupArgs(appDeploy)
        }
        .toList
        .distinct

    RawDeployArgs(resolvedArgs, resolvedArgs.map(ParsedDeployArg.parse))

  }

  private def resolveRawSetupArgs(appDeploy: AppDeploy): Iterable[String] = {
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
              .map { d => z"dns@${d}" }

          val caddy =
            Iterable(
              z"caddy@${appDeploy.resolvedApp.server.name}",
            )

          dns ++ caddy

      }

    val supervisor: Option[String] =
      descriptor.launcher match {
        case sd: SupervisorDescriptor =>
          Some(z"supervisor@${appDeploy.resolvedApp.server.name}")
        case _ =>
          None
      }

    val database: Option[String] =
      descriptor.setup.database match {
        case Some(dbs) =>
          Some(z"database@${descriptor.resolvedDomainNames.head}")
        case None =>
          None
      }

    caddy ++ supervisor ++ database ++ Some(z"${descriptor.resolvedDomainNames.head}")

  }


}
