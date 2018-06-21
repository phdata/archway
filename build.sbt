name := "heimdali-api"

version := "2018.08.01"

scalaVersion := "2.12.5"

resolvers += "Cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos"

val cdhVersion = "cdh5.11.1"
val hiveVersion = s"1.1.0-$cdhVersion"
val hadoopVersion = s"2.6.0-$cdhVersion"

val circeVersion = "0.9.3"
val doobieVersion = "0.5.3"
val catsVersion = "1.1.0"
val http4sVersion = "0.18.11"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,

  "co.fs2" %% "fs2-core" % "0.10.4",

  "org.tpolecat" %% "doobie-core"        % doobieVersion,
  "org.tpolecat" %% "doobie-postgres"    % doobieVersion,
  "org.tpolecat" %% "doobie-scalatest"   % doobieVersion % Test,

  "com.github.pureconfig" %% "pureconfig" % "0.9.1",

  "org.typelevel" %% "cats-effect" % "0.10.1",
  "org.typelevel" %% "cats-core" % catsVersion,
  "com.casualmiracles" %% "treelog-cats" % "1.4.4",

  "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "io.circe" %% "circe-optics" % circeVersion,
  "io.circe" %% "circe-java8" % circeVersion,

  ("com.pauldijou" %% "jwt-core" % "0.14.1").exclude("org.bouncycastle", "bcpkix-jdk15on"),
  ("com.pauldijou" %% "jwt-circe" % "0.14.1").exclude("org.bouncycastle", "bcpkix-jdk15on"),
  "org.bouncycastle" % "bcpkix-jdk15on" % "1.57",

  "com.unboundid" % "unboundid-ldapsdk" % "4.0.0",

  "org.flywaydb" % "flyway-core" % "4.2.0",
  "postgresql" % "postgresql" % "9.0-801.jdbc4",
  "mysql" % "mysql-connector-java" % "6.0.6",

  "org.apache.hadoop" % "hadoop-client" % hadoopVersion,
  "org.apache.hive" % "hive-jdbc" % hiveVersion,
  "org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-common" % hadoopVersion % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-client" % hadoopVersion % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-minicluster" % hadoopVersion % Test,

  "org.mockito" % "mockito-core" % "2.18.3" % Test,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test)

assemblyJarName in assembly := "heimdali-api.jar"

parallelExecution in Test := false

addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.7" cross CrossVersion.binary)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")

scalacOptions ++= Seq("-Ypartial-unification", "-language:higherKinds")

scalaVersion in ThisBuild := "2.12.5"
