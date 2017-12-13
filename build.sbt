name := "heimdali-api"

version := "2018.08.01"

scalaVersion := "2.12.1"

lazy val root = (project in file("."))

val akkaVersion = "2.5.6"
val circeVersion = "0.8.0"
val akkaHttpVersion = "10.0.10"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-java8" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,

  "de.heikoseeberger" %% "akka-http-circe" % "1.18.0",
  "com.pauldijou" %% "jwt-core" % "0.14.1",
  "com.pauldijou" %% "jwt-circe" % "0.14.1",

  "com.unboundid" % "unboundid-ldapsdk" % "4.0.0",

  "io.getquill" %% "quill" % "1.4.0",
  "org.postgresql" % "postgresql" % "42.1.4",
  "org.flywaydb" % "flyway-core" % "4.2.0",

  "com.typesafe.akka" %% "akka-slf4j" % "2.5.3",
  "org.apache.hadoop" % "hadoop-client" % "2.6.0" % "provided" excludeAll(ExclusionRule(organization = "org.slf4j"),
                                                                          ExclusionRule(organization = "com.google.guava")),

  "com.whisk" %% "docker-testkit-scalatest" % "0.9.5" % Test,
  "com.whisk" %% "docker-testkit-impl-docker-java" % "0.9.5" % Test,
  "com.whisk" %% "docker-testkit-config" % "0.9.5" % Test,
  "com.novocode" % "junit-interface" % "0.11" % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "org.mockito" % "mockito-core" % "2.+" % Test,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test)

libraryDependencies ++= Seq(
  "org.apache.hadoop" % "hadoop-hdfs" % "2.6.0" % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-common" % "2.6.0" % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-client" % "2.6.0" % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-minicluster" % "2.6.0" % Test
).map(_.excludeAll(ExclusionRule(organization = "javax.servlet"),
  ExclusionRule(organization = "com.google.guava")))

dependencyOverrides ++= Set(
  "com.google.guava" % "guava" % "19.0"
)

packageName in Universal := "heimdali-api"

parallelExecution in Test := false