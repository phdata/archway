import sbt._

object Dependencies {

  val http4sVersion = "0.20.0-M6"
  val http4s = Seq(
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % http4sVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion
  )

  val fs2HttpVersion = "0.4.0"
  val fs2Http = Seq(
    "com.spinoco" %% "fs2-http" % fs2HttpVersion
  )

  val fs2Version = "1.0.0"
  val fs2 = Seq(
    "co.fs2" %% "fs2-core" % fs2Version
  )

  val scalatagsVersion = "0.6.7"
  val scalatags = Seq(
    "com.lihaoyi" %% "scalatags" % scalatagsVersion
  )

  val mailerVersion = "1.0.0"
  val mailer = Seq(
    "com.github.daddykotex" %% "courier" % mailerVersion exclude("com.sun.mail", "javax.mail"),
    "org.jvnet.mock-javamail" % "mock-javamail" % "1.9" % "test"
  )

  val doobieVersion = "0.6.0"
  val doobie = Seq(
    "org.tpolecat" %% "doobie-core" % doobieVersion,
    "org.tpolecat" %% "doobie-postgres" % doobieVersion,
    "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test
  )

  val pureConfigVersion = "0.9.1"
  val pureConfig = Seq(
    "com.github.pureconfig" %% "pureconfig" % pureConfigVersion
  )

  val catsVersion = "1.3.0"
  val cats = Seq(
    "org.typelevel" %% "cats-core" % catsVersion withSources()
  )

  val catsEffectVersion = "1.0.0"
  val catsEffect = Seq(
    "org.typelevel" %% "cats-effect" % catsEffectVersion withSources()
  )

  val circeVersion = "0.11.0"
  val circe = Seq(
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    "io.circe" %% "circe-generic-extras" % circeVersion,
    "io.circe" %% "circe-optics" % circeVersion,
    "io.circe" %% "circe-java8" % circeVersion
  )

  val jwtVersion = "2.0.0"
  val jwt = Seq(
    ("com.pauldijou" %% "jwt-core" % jwtVersion)
      .exclude("org.bouncycastle", "bcpkix-jdk15on"),
    ("com.pauldijou" %% "jwt-circe" % jwtVersion)
      .exclude("org.bouncycastle", "bcpkix-jdk15on")
  )

  val cdhVersion = "cdh5.13.0"
  val hiveVersion = s"1.1.0-$cdhVersion"
  val hadoopVersion = s"2.6.0-$cdhVersion"
  val sentryVersion = s"1.5.1-$cdhVersion"
  val hadoop = Seq(
    "org.apache.sentry" % "sentry-provider-db" % sentryVersion % "provided",
    "org.apache.hadoop" % "hadoop-client" % hadoopVersion % "provided",
    "org.apache.hive" % "hive-jdbc" % hiveVersion % "provided",
    "org.apache.kafka" %% "kafka" % "0.10.1.1",
    "org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion % Test classifier "" classifier "tests",
    "org.apache.hadoop" % "hadoop-common" % hadoopVersion % Test classifier "" classifier "tests",
    "org.apache.hadoop" % "hadoop-client" % hadoopVersion % Test classifier "" classifier "tests",
    "org.apache.hadoop" % "hadoop-minicluster" % hadoopVersion % Test
  )

  val coreTest = Seq(
    "org.mockito" % "mockito-core" % "2.18.3" % Test,
    "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test,
    "org.powermock" % "powermock-core" % "1.7.4" % Test
  )

  val scalacheckVersion = "1.14.0"
  val scalacheck = Seq(
    "org.scalacheck" %% "scalacheck" % scalacheckVersion % Test
  )

  val bouncy = Seq(
    "org.bouncycastle" % "bcpkix-jdk15on" % "1.57" % "provided"
  )

  val logging = Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0",
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )

  val unbound = Seq(
    "com.unboundid" % "unboundid-ldapsdk" % "4.0.0"
  )

  val dbCore = Seq(
    "org.flywaydb" % "flyway-core" % "4.2.0",
    "postgresql" % "postgresql" % "9.0-801.jdbc4" % "provided",
    "mysql" % "mysql-connector-java" % "6.0.6" % "provided"
  )

  val iniVersion = "0.5.1"
  val iniConfig = Seq(
    "org.ini4j" % "ini4j" % iniVersion
  )

  def exclusions(module: ModuleID): ModuleID =
    module.excludeAll(
      ExclusionRule(organization = "org.slf4j"),
      ExclusionRule(organization = "com.sun.jdmk"),
      ExclusionRule(organization = "com.sun.jmx"),
      ExclusionRule(organization = "javax.jms")
    )

  val modelsDependencies =
    (cats ++ catsEffect ++ circe ++ doobie)
      .map(exclusions)

  val apiDependencies =
    (coreTest ++ dbCore ++ logging ++ bouncy ++ pureConfig ++
      http4s ++ fs2 ++ doobie ++ cats ++ catsEffect ++ circe ++ hadoop)
      .map(exclusions)

  val configDependencies =
    pureConfig
      .map(exclusions)

  val commonDependencies =
    (unbound ++ mailer ++ logging ++ doobie ++ cats ++ catsEffect ++ fs2 ++ pureConfig ++ circe ++
      scalatags ++ hadoop ++ fs2Http ++ http4s ++ jwt ++ iniConfig ++ scalacheck ++ coreTest ++ bouncy ++
      Seq("org.typelevel" %% "jawn-parser" % "0.14.0"))
      .map(exclusions)

  val provisioningDependencies =
    (coreTest ++ hadoop)
      .map(exclusions)
}