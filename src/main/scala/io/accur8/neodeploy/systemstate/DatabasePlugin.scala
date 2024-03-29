package io.accur8.neodeploy.systemstate


import io.accur8.neodeploy.{DatabaseSetupMixin}
import io.accur8.neodeploy.model.DomainName
import io.accur8.neodeploy.systemstate.SystemState.DatabaseSetup
import io.accur8.neodeploy.systemstate.SystemStateModel.M
import io.accur8.neodeploy.SharedImports.*
import io.accur8.neodeploy.resolvedmodel.ResolvedApp

object DatabasePlugin {

  def systemState(resolvedApp: ResolvedApp): M[DatabaseSetup] = {
//    val domainName = DomainName(infraDeploy.originalArg.serverPart.get.value)
//    val resolvedAppOpt =
//      infraDeploy
//        .resolvedRepo
//        .applications
//        .find(_.descriptor.resolvedDomainNames.contains(domainName))
//
//    resolvedAppOpt match {
//      case None =>
//        zfail(new RuntimeException(s"no application found for domain name ${domainName}"))
//      case Some(resolvedApp) =>
        resolvedApp.descriptor.setup.database match {
          case None =>
            zfail(new RuntimeException(s"application ${resolvedApp.qname} has no database setup configured"))
          case Some(databaseSetup) =>
            for {
              generatedProps <- DatabaseSetupMixin.loadOrGenerateProperties(databaseSetup, resolvedApp.gitDirectory)
            } yield
              DatabaseSetup(
                databaseSetup.databaseServer,
                databaseSetup.databaseName,
                databaseSetup.owner,
                generatedProps.databasePasswords,
                databaseSetup.extraUsers,
              )
        }
//    }
  }

  //  def run: Z[Unit] = {
  //    for {
  //      superUserConn <- superUserConnM
  //      databaseExists <- databaseExists(superUserConn)
  //      _ <-
  //        if ( databaseExists ) {
  //          loggerF.info(s"database ${databaseName} on server ${databaseServer} already exists no more setup")
  //        } else {
  //          for {
  //            _ <- createDatabase(superUserConn)
  //            ownerConn <- ownerConnM
  //            _ <- DatabaseSetupMixin.updateSecretsWithDatabaseConfig(gitAppDirectory, ownerConn.config)
  //            _ <- extraUsers.map(createUser(_)(ownerConn, superUserConn)).sequence
  //            //      _ <- databaseSetupDescriptor.zooFiles.map(createTablesAndQubes(_, ownerConn)).sequence
  //          } yield ()
  //        }
  //    } yield ()
  //  }

}
