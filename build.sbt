
// 
// DO NOT EDIT THIS FILE IT IS MACHINE GENERATED
// 
// This file is generated from modules.conf using `a8-versions build_dot_sbt`
// 
// It was generated at 2022-11-19T06:03:37.783520 by glen on stella.local
// 
// a8-versions build/versioning info follows
// 
//        build_date : Thu Nov 17 17:09:32 EST 2022
//        build_machine : stella.hsd1.nh.comcast.net
//        build_user : glen
// 
//      

val appVersion = a8.sbt_a8.versionStamp(file("."))
val syncVersion = "1.0.0-20230918_1936_glenlogboot"
val scalaLibVersion = "3.3.0"
val zeroWasteVersion = "0.2.12"

val zeroWastePlugin = compilerPlugin("com.github.ghik" % "zerowaste" % zeroWasteVersion cross CrossVersion.full)

Global / scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

Global / resolvers += "a8-repo" at Common.readRepoUrl()
Global / publishTo := Some("a8-repo-releases" at Common.readRepoUrl())
Global / credentials += Common.readRepoCredentials()

//Global / publishTo := sonatypePublishToBundle.value
//Global / credentials += Credentials(Path.userHome / ".sbt" / "sonatype.credentials")


Global / scalaVersion := scalaLibVersion

Global / organization := "io.accur8"

Global / version := appVersion

Global / versionScheme := Some("strict")

Global / serverConnectionType := ConnectionType.Local

Global / scalacOptions ++= Seq(
  "-encoding", "UTF-8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps",
  "-language:strictEquality",
  // "-Werror",
)


lazy val versions =
  Common
    .jvmProject("a8-versions", file("."), "versions")
    .settings(
      libraryDependencies ++= Seq(

        zeroWastePlugin,

        ("io.get-coursier" %% "coursier" % "2.1.4").cross(CrossVersion.for3Use2_13)
          exclude("org.scala-lang.modules", "scala-collection-compat_2.13")
          exclude("org.scala-lang.modules", "scala-xml_2.13")
        ,

        ("io.get-coursier" %% "coursier-cache" % "2.1.2").cross(CrossVersion.for3Use2_13)
          exclude("org.scala-lang.modules", "scala-collection-compat_2.13")
          exclude("org.scala-lang.modules", "scala-xml_2.13")
        ,

        "org.scala-lang.modules" %% "scala-xml" % "2.1.0",

        "com.softwaremill.sttp.client3" %% "core" % "3.8.15",
        "com.lihaoyi" %% "fastparse" % "3.0.1",
        "io.accur8" %% "a8-sync-api" % syncVersion,
        "org.rogach" %% "scallop"    % "4.1.0",
        "dev.zio" %% "zio-process"   % "0.7.2",
        "org.typelevel" %% "cats-parse" % "0.3.8",

        "dev.zio" %% "zio-nio"     % "2.0.1",
        "dev.zio" %% "zio-rocksdb" % "0.4.3",

        "software.amazon.awssdk" % "apache-client" % "2.19.6",
        "software.amazon.awssdk" % "route53" % "2.19.6",

        "org.scalatest" %% "scalatest" % "3.2.12" % "test",

      )
    )


   
