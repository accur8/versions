
// 
// DO NOT EDIT THIS FILE IT IS MACHINE GENERATED
// 
// This file is generated from modules.conf using `a8-versions build_dot_sbt`
// 
// It was generated at 2020-06-15 13:26:28.541 -0400 by glen on ROAR
// 
// a8-versions build/versioning info follows
// 
// 
// 
//      

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.6.1")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.31")
//addSbtPlugin("io.get-coursier" % "sbt-coursier" % "2.0.0-RC6")
//addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.0")

addSbtPlugin("com.frugalmechanic" % "fm-sbt-s3-resolver" % "0.19.0")

resolvers += "a8-sbt-plugins" at readRepoUrl()
credentials += readRepoCredentials()

//libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.21"
//addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.9.3")

addSbtPlugin("a8" % "sbt-a8" % "1.1.0-20191220_1208")

addSbtPlugin("ch.epfl.scala" % "sbt-bloop" % "1.0.0-M10")

// This plugin can be removed when using Scala 2.13.0 or above
addSbtPlugin("org.lyranthe.sbt" % "partial-unification" % "1.1.2")




  def readRepoUrl() = readRepoProperty("repo_url")

  lazy val repoConfigFile = new java.io.File(System.getProperty("user.home") + "/.a8/repo.properties")

  lazy val repoProperties = {
    import scala.collection.JavaConverters._
    val props = new java.util.Properties()
    if ( repoConfigFile.exists() ) {
      val input = new java.io.FileInputStream(repoConfigFile)
      try {
        props.load(input)
      } finally {
        input.close()
      }
      props.asScala
    } else {
      sys.error("config file " + repoConfigFile + " does not exist")
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

  def readRepoCredentials(): Credentials = {
    val repoUrl = new java.net.URL(readRepoUrl())
    Credentials(
      readRepoProperty("repo_realm"),
      repoUrl.getHost,
      readRepoProperty("repo_user"),
      readRepoProperty("repo_password"),
    )
  }


  

