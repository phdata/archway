name := "heimdali-api"

version := "2018.08.01"

scalaVersion := "2.12.1"

resolvers += "Cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos"

val akkaVersion = "2.5.6"
val circeVersion = "0.8.0"
val akkaHttpVersion = "10.0.10"
val cdhVersion = "cdh5.11.1"
val hiveVersion = s"1.1.0-$cdhVersion"
val hadoopVersion = s"2.6.0-$cdhVersion"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,

  "ch.megard" %% "akka-http-cors" % "0.2.2",

  "org.scalikejdbc" %% "scalikejdbc" % "3.1.0",
  "org.scalikejdbc" %% "scalikejdbc-config" % "3.1.0",

  "com.github.dnvriend" %% "akka-persistence-inmemory" % "2.5.1.1",

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-java8" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,

  "de.heikoseeberger" %% "akka-http-circe" % "1.18.0",
  "com.pauldijou" %% "jwt-core" % "0.14.1",
  "com.pauldijou" %% "jwt-circe" % "0.14.1",

  "com.unboundid" % "unboundid-ldapsdk" % "4.0.0",

  "io.getquill" %% "quill-async-postgres" % "2.3.1",
  "org.flywaydb" % "flyway-core" % "4.2.0",
  "org.postgresql" % "postgresql" % "42.1.4",

  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "org.apache.hadoop" % "hadoop-client" % hadoopVersion,
  "org.apache.hive" % "hive-jdbc" % hiveVersion,

  "com.novocode" % "junit-interface" % "0.11" % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "org.mockito" % "mockito-core" % "2.+" % Test,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test,

  "org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-common" % hadoopVersion % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-client" % hadoopVersion % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-minicluster" % hadoopVersion % Test)

dependencyOverrides ++= Set(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "io.netty" % "netty-handler" % "4.1.12.Final",
  "com.twitter" %% "finagle-core" % "7.0.0"
)

assemblyJarName in assembly := "heimdali-api.jar"

parallelExecution in Test := false