name := "heimdali-api"

version := "2018.08.01"

scalaVersion := "2.12.5"

resolvers += "Cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos"

resolvers += "Apache" at "http://repo.spring.io/plugins-release/"

fullResolvers := ("Jboss" at "https://repository.jboss.org/maven2") +: resolvers.value

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

val cdhVersion = "cdh5.13.0"
val hiveVersion = s"1.1.0-$cdhVersion"
val hadoopVersion = s"2.6.0-$cdhVersion"
val sentryVersion = s"1.5.1-$cdhVersion"


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
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test,
  "com.github.pureconfig" %% "pureconfig" % "0.9.1",
  "org.typelevel" %% "cats-effect" % "0.10.1",
  "org.typelevel" %% "cats-core" % catsVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "io.circe" %% "circe-optics" % circeVersion,
  "io.circe" %% "circe-java8" % circeVersion,
  ("org.apache.kafka" %% "kafka" % "0.10.1.1")
    .exclude("org.slf4j", "slf4j-log4j12")
    .exclude("com.sun.jmx", "jmxri")
    .exclude("com.sun.jdmk", "jmxtools"),
  ("com.pauldijou" %% "jwt-core" % "0.14.1")
    .exclude("org.bouncycastle", "bcpkix-jdk15on"),
  ("com.pauldijou" %% "jwt-circe" % "0.14.1")
    .exclude("org.bouncycastle", "bcpkix-jdk15on"),
  "com.github.mpilquist" %% "simulacrum" % "0.12.0",
  "org.bouncycastle" % "bcpkix-jdk15on" % "1.57" % "provided",
  "com.unboundid" % "unboundid-ldapsdk" % "4.0.0",
  "org.flywaydb" % "flyway-core" % "4.2.0",
  "postgresql" % "postgresql" % "9.0-801.jdbc4" % "provided",
  "mysql" % "mysql-connector-java" % "6.0.6" % "provided",
  "org.apache.sentry" % "sentry-provider-db" % sentryVersion % "provided",
  "org.apache.hadoop" % "hadoop-client" % hadoopVersion % "provided",
  "org.apache.hive" % "hive-jdbc" % hiveVersion % "provided",
  "org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-common" % hadoopVersion % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-client" % hadoopVersion % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-minicluster" % hadoopVersion % Test,
  "org.mockito" % "mockito-core" % "2.18.3" % Test,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test,
  "org.powermock" % "powermock-core" % "1.7.4" % Test
)

assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last endsWith "-site.xml" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

assemblyJarName in assembly := "heimdali-api.jar"

parallelExecution in Test := false

unmanagedClasspath in Test ++= Seq(
  baseDirectory.value / "sentry-conf",
  baseDirectory.value / "hive-conf"
)

addCompilerPlugin(
  "org.spire-math" % "kind-projector" % "0.9.7" cross CrossVersion.binary
)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")

scalacOptions ++= Seq("-Ypartial-unification", "-language:higherKinds")

mainClass in reStart := Some("com.heimdali.Main")

Revolver.enableDebugging(port = 5050, suspend = true)

javaOptions in reStart := Seq(
  "-Dhadoop.home.dir=$PWD",
  s"-Djava.security.krb5.conf=${baseDirectory.value}/krb5.conf"
)

envVars in reStart := Map(
  "HEIMDALI_SECRET" -> """r8,q8vx~AnU^U"W.""",
  "HEIMDALI_REST_PORT" -> "8080",

  "HEIMDALI_HDFS_USER_ROOT" -> "/user",
  "HEIMDALI_HDFS_DS_ROOT" -> "/data/governed",
  "HEIMDALI_HDFS_SW_ROOT" -> "/data/shared_workspaces",
  "HEIMDALI_HDFS_USER_SIZE" -> "1",
  "HEIMDALI_HDFS_SHARED_SIZE" -> "1",
  "HEIMDALI_HDFS_DATASET_SIZE" -> "1",

  "HEIMDALI_CLUSTER_ENVIRONMENT" -> "dev",

  "HEIMDALI_LDAP_HOST" -> "ad1.jotunn.io",
  "HEIMDALI_LDAP_PORT" -> "636",
  "HEIMDALI_LDAP_ADMIN_DN" -> "CN=Administrator,CN=Users,DC=jotunn,DC=io",
  "HEIMDALI_LDAP_ADMIN_PASS" -> "Jotunn123!",
  "HEIMDALI_LDAP_BASE_DN" -> "DC=jotunn,DC=io",
  "HEIMDALI_LDAP_GROUP_PATH" -> "ou=heimdali,DC=jotunn,DC=io",

  "HEIMDALI_REALM" -> "JOTUNN.IO",

  "HEIMDALI_HIVE_URL" -> "jdbc:hive2://master1.jotunn.io:10000/default;principal=hive/_HOST@JOTUNN.IO;",

  "HEIMDALI_DB_URL" -> "jdbc:postgresql://localhost/heimdali",
  "HEIMDALI_DB_USER" -> "postgres",
  "HEIMDALI_DB_PASS" -> "postgres",
  "HEIMDALI_DB_DRIVER" -> "org.postgresql.Driver",

  "HEIMDALI_CLUSTER_NAME" -> "cluster",

  "HEIMDALI_CM_URL_BASE" -> "http://master1.jotunn.io:7180",
  "HEIMDALI_CM_ADMIN_USER" -> "admin",
  "HEIMDALI_CM_ADMIN_PASSWORD" -> "admin",

  "HEIMDALI_KEYTAB_REFRESH" -> "1h",

  "HEIMDALI_INFRA_APPROVERS" -> "CN=approvers,CN=Groups,DC=jotunn,DC=io",
  "HEIMDALI_RISK_APPROVERS" -> "CN=approvers,CN=Groups,DC=jotunn,DC=io",

  "HEIMDALI_YARN_USER_CORES" -> "1",
  "HEIMDALI_YARN_USER_MEMORY" -> "1",
  "HEIMDALI_YARN_SHARED_CORES" -> "1",
  "HEIMDALI_YARN_SHARED_MEMORY" -> "1",
  "HEIMDALI_YARN_DATASET_CORES" -> "1",
  "HEIMDALI_YARN_DATASET_MEMORY" -> "1",
  "HEIMDALI_YARN_USER_PARENTS" -> "root.users",
  "HEIMDALI_YARN_SHARED_PARENTS" -> "root",
  "HEIMDALI_YARN_DATASET_PARENTS" -> "root",

  "ZK_QUORUM" -> "master1.jotunn.io:2181",
  "HEIMDALI_API_SERVICE_PRINCIPAL" -> "benny@JOTUNN.IO"
)
