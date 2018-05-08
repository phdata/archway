name := "heimdali-api"

version := "2018.08.01"

scalaVersion := "2.12.5"

resolvers += "Cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos"

val akkaVersion = "2.5.6"
val circeVersion = "0.9.2"
val akkaHttpVersion = "10.0.10"
val cdhVersion = "cdh5.11.1"
val hiveVersion = s"1.1.0-$cdhVersion"
val hadoopVersion = s"2.6.0-$cdhVersion"
val doobieVersion = "0.5.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,

  "ch.megard" %% "akka-http-cors" % "0.2.2",
  "de.heikoseeberger" %% "akka-http-circe" % "1.18.0",

  "com.github.dnvriend" %% "akka-persistence-inmemory" % "2.5.1.1",

  "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,

  ("com.pauldijou" %% "jwt-core" % "0.14.1").exclude("org.bouncycastle", "bcpkix-jdk15on"),
  ("com.pauldijou" %% "jwt-circe" % "0.14.1").exclude("org.bouncycastle", "bcpkix-jdk15on"),
  "org.bouncycastle" % "bcpkix-jdk15on" % "1.57" % "provided",

  "com.unboundid" % "unboundid-ldapsdk" % "4.0.0",

  "org.scalikejdbc" %% "scalikejdbc" % "2.5.2",

  "org.flywaydb" % "flyway-core" % "4.2.0",
  "postgresql" % "postgresql" % "9.0-801.jdbc4" % "provided",
  "mysql" % "mysql-connector-java" % "6.0.6" % "provided",

  "org.apache.hadoop" % "hadoop-client" % hadoopVersion % "provided",
  "org.apache.hive" % "hive-jdbc" % hiveVersion % "provided",
  "org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-common" % hadoopVersion % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-client" % hadoopVersion % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-minicluster" % hadoopVersion % Test,

  "org.mockito" % "mockito-core" % "2.+" % Test,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test)

dependencyOverrides ++= Set(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "io.netty" % "netty-handler" % "4.1.12.Final",
  "com.twitter" %% "finagle-core" % "7.0.0"
)

assemblyJarName in assembly := "heimdali-api.jar"

parallelExecution in Test := false