import sbt.{ExclusionRule, _}

object Dependencies {

  val http4sVersion = "0.20.6"

  val http4s = Seq(
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % http4sVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion
  )

  val scalatagsVersion = "0.6.7"

  val scalatags = Seq(
    "com.lihaoyi" %% "scalatags" % scalatagsVersion
  )

  val mailerVersion = "1.0.0"
  val mailer = Seq("com.github.daddykotex" %% "courier" % mailerVersion)

  val attoVersion = "0.6.5"

  val atto = Seq(
    "org.tpolecat" %% "atto-core" % attoVersion
  )

  val doobieVersion = "0.6.0"

  val doobie = Seq(
    "org.tpolecat" %% "doobie-core" % doobieVersion,
    "org.tpolecat" %% "doobie-postgres" % doobieVersion,
    "org.tpolecat" %% "doobie-hikari" % doobieVersion,
    "org.tpolecat" %% "doobie-scalatest" % doobieVersion % "test"
  )

  val catsVersion = "1.3.1"

  val cats = Seq(
    "org.typelevel" %% "cats-core" % catsVersion withSources ()
  )

  val catsEffectVersion = "1.3.1"

  val catsEffect = Seq(
    "org.typelevel" %% "cats-effect" % catsEffectVersion withSources ()
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

  val circeConfigVersion = "0.6.1"

  val circeConfig = Seq(
    "io.circe" %% "circe-config" % circeConfigVersion
  )

  val jwtVersion = "2.1.0"

  val jwt = Seq(
    "com.pauldijou" %% "jwt-core" % jwtVersion,
    "com.pauldijou" %% "jwt-circe" % jwtVersion
  )

  val hiveVersion = s"3.1.3000.7.1.4.0-203"

  val hive = Seq(
    "org.apache.hive" % "hive-jdbc" % hiveVersion % "provided"
  )

  val coreTest = Seq(
    "org.mockito" % "mockito-core" % "2.18.3" % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % "test",
    "org.powermock" % "powermock-core" % "1.7.4" % "test"
  )

  val scalateVersion = "1.9.4"

  val scalate = Seq(
    "org.scalatra.scalate" %% "scalate-core" % scalateVersion
  )

  val scalaLoggingVersion = "3.8.0"
  val log4j2Version = "2.14.0"

  val logging = Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
    "org.apache.logging.log4j" % "log4j-api" % log4j2Version
  )

  Seq()

  val unboundIdVersion = "4.0.11"

  val unbound = Seq(
    "com.unboundid" % "unboundid-ldapsdk" % unboundIdVersion
  )

  val dbCore = Seq(
    "org.postgresql" % "postgresql" % "9.4.1212" % "provided",
    "mysql" % "mysql-connector-java" % "6.0.6" % "provided"
  )

  val iniVersion = "0.5.1"

  val iniConfig = Seq(
    "org.ini4j" % "ini4j" % iniVersion
  )

  val simulacrumVersion = "0.17.0"

  val simulacrum = Seq(
    "com.github.mpilquist" %% "simulacrum" % simulacrumVersion
  )

  def exclusions(module: ModuleID): ModuleID =
    module.excludeAll(
      ExclusionRule(organization = "com.sun.jdmk"),
      ExclusionRule(organization = "com.sun.jmx"),
      ExclusionRule(organization = "javax.jms"),
      ExclusionRule(organization = "commons-beanutils"),
      ExclusionRule(organization = "commons-logging"),
      ExclusionRule(organization = "org.apache.derby")
    )

  val apiDependencies =
    (coreTest ++ dbCore ++ logging ++ circeConfig ++
        http4s ++ doobie ++ cats ++ catsEffect ++ circe ++ hive).map(exclusions)

  val commonDependencies =
    (scalate ++ unbound ++ mailer ++ logging ++ doobie ++ cats ++ catsEffect ++ circeConfig ++ circe ++
        scalatags ++ hive ++ http4s ++ jwt ++ iniConfig ++ coreTest ++ atto
      ++ Seq("org.typelevel" %% "jawn-parser" % "0.14.0")).map(exclusions)

  val provisioningDependencies =
    (coreTest ++ hive).map(exclusions)

  val integrationTestDependencies =
    (dbCore ++
        Seq(
          "org.tpolecat" %% "doobie-scalatest" % doobieVersion,
          "org.mockito" % "mockito-core" % "2.18.3" % "test",
          "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0",
          "org.powermock" % "powermock-core" % "1.7.4",
          "org.apache.hive" % "hive-jdbc" % hiveVersion % "provided"
        )).map(exclusions)
}
