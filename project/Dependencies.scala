import sbt._

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
  val mailer = Seq(
    "com.github.daddykotex" %% "courier" % mailerVersion)
  
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
    "org.typelevel" %% "cats-core" % catsVersion withSources()
  )

  val catsEffectVersion = "1.3.1"
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

  val circeConfigVersion = "0.6.1"
  val circeConfig = Seq(
    "io.circe" %% "circe-config" % circeConfigVersion
  )

  val jwtVersion = "2.1.0"
  val jwt = Seq(
    "com.pauldijou" %% "jwt-core" % jwtVersion,
    "com.pauldijou" %% "jwt-circe" % jwtVersion
  )

  val cdhVersion = "cdh5.13.0"
  val hiveVersion = s"1.1.0-$cdhVersion"
  val hadoopVersion = s"2.6.0-$cdhVersion"
  val sentryVersion = s"1.5.1-$cdhVersion"
  val hadoop = Seq(
    "org.apache.sentry" % "sentry-provider-db" % sentryVersion excludeAll(
      ExclusionRule("org.apache.hive"),
      ExclusionRule("org.eclipse.jetty.orbit"),
      ExclusionRule("commons-beanutils", "commons-beanutils-core"),
      ExclusionRule("org.datanucleus"),
      ExclusionRule("commons-beanutils"),
      ExclusionRule("org.apache.hadoop", "hadoop-yarn-common"),
      ExclusionRule("org.slf4j")
    ),
    "org.apache.hadoop" % "hadoop-client" % hadoopVersion % "provided",
    "org.apache.hive" % "hive-jdbc" % hiveVersion % "provided",
    "org.apache.kafka" %% "kafka" % "0.10.1.1" excludeAll ExclusionRule(organization = "org.slf4j"),
    "org.apache.hadoop" % "hadoop-common" % hadoopVersion % "test" classifier "" classifier "tests",
    "org.apache.hadoop" % "hadoop-client" % hadoopVersion % "test" classifier "" classifier "tests",
    "org.apache.hadoop" % "hadoop-minicluster" % hadoopVersion % "test"
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
  
  val bouncyVersion = "1.57"
  val bouncy = Seq(
    "org.bouncycastle" % "bcpkix-jdk15on" % bouncyVersion % "provided"
  )

  val loggingVersion = "3.8.0"
  val logging = Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % loggingVersion
  )

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
  
  val pac4jKerberosVersion = "3.7.0"
  val pac4jKerberos = Seq("org.pac4j" % "pac4j-kerberos" % pac4jKerberosVersion)

  val kerb4jClientVersion = "0.0.8"
  val kerb4jClient = Seq("com.kerb4j" % "kerb4j-client" % kerb4jClientVersion % "test")

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
      ExclusionRule(organization = "commons-logging")
    )

  val apiDependencies =
    (coreTest ++ dbCore ++ logging ++ bouncy ++ circeConfig ++
      http4s ++ doobie ++ cats ++ catsEffect ++ circe ++ hadoop)
      .map(exclusions)

  val commonDependencies =
    (scalate ++ unbound ++ mailer ++ logging ++ doobie ++ cats ++ catsEffect ++ circeConfig ++ circe ++
      scalatags ++ hadoop ++ http4s ++ jwt ++ iniConfig ++ coreTest ++ bouncy ++ atto ++ pac4jKerberos  ++ kerb4jClient
      ++ Seq("org.typelevel" %% "jawn-parser" % "0.14.0"))
      .map(exclusions)

  val provisioningDependencies =
    (coreTest ++ hadoop ++ simulacrum)
      .map(exclusions)
  
  val integrationTestDependencies =
    (dbCore ++ kerb4jClient ++
    Seq("org.tpolecat" %% "doobie-scalatest" % doobieVersion,
        "org.mockito" % "mockito-core" % "2.18.3" % "test",
        "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0",
        "org.powermock" % "powermock-core" % "1.7.4",
        "org.apache.hive" % "hive-jdbc" % hiveVersion % "provided")).map(exclusions)
}
