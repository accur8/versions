package a8.versions

import a8.common.logging.Logging

import scala.collection.mutable


object RepoAssist extends Logging {

  import coursier.credentials.Credentials

  def readRepoUrl() = readRepoProperty("repo_url")

  lazy val repoConfigFile = new java.io.File(System.getProperty("user.home") + "/.a8/repo.properties")

  lazy val repoProperties: Map[String, String] = {
    import scala.jdk.CollectionConverters._
    val props = new java.util.Properties()
    if ( repoConfigFile.exists() ) {
      val input = new java.io.FileInputStream(repoConfigFile)
      try {
        props.load(input)
      } finally {
        input.close()
      }
      props.asScala.toMap
    } else {
      logger.debug(s"Repo config file not found: ${repoConfigFile.getAbsolutePath}")
      Map()
    }
  }

  def readRepoProperty(propertyName: String): String = {
    repoProperties.get(propertyName) match {
      case Some(s) =>
        s
      case None =>
        sys.error("could not find property " + propertyName + " in " + repoConfigFile)
    }
  }

  def readRepoPropertyOpt(propertyName: String): Option[String] = {
    repoProperties.get(propertyName)
  }


  //  def readRepoCredentials(): Credentials = {
//    val repoUrl = new java.net.URL(readRepoUrl())
//    Credentials(
//      readRepoProperty("repo_realm"),
//      repoUrl.getHost,
//      readRepoProperty("repo_user"),
//      readRepoProperty("repo_password"),
//    )
//  }

}
