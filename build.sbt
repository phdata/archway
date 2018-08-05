name := "heimdali-api"

version := "2018.08.01"

scalaVersion := "2.12.5"

resolvers += "Cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos"

resolvers += "Apache" at "http://repo.spring.io/plugins-release/"

resolvers += "Jboss" at "https://repository.jboss.org/nexus/content/groups/public"

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
  "org.apache.kafka" %% "kafka" % "0.10.1.1",
  ("com.pauldijou" %% "jwt-core" % "0.14.1")
    .exclude("org.bouncycastle", "bcpkix-jdk15on"),
  ("com.pauldijou" %% "jwt-circe" % "0.14.1")
    .exclude("org.bouncycastle", "bcpkix-jdk15on"),
  "com.github.mpilquist" %% "simulacrum" % "0.12.0",
  "org.bouncycastle" % "bcpkix-jdk15on" % "1.57", // % "provided",
  "com.unboundid" % "unboundid-ldapsdk" % "4.0.0",
  "org.flywaydb" % "flyway-core" % "4.2.0",
  "postgresql" % "postgresql" % "9.0-801.jdbc4", // % "provided",
  "mysql" % "mysql-connector-java" % "6.0.6", // % "provided",
  "org.apache.sentry" % "sentry-provider-db" % sentryVersion, // % "provided",
  "org.apache.hadoop" % "hadoop-client" % hadoopVersion, // % "provided",
  "org.apache.hive" % "hive-jdbc" % hiveVersion, // % "provided",
  "org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-common" % hadoopVersion % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-client" % hadoopVersion % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-minicluster" % hadoopVersion % Test,
  "org.mockito" % "mockito-core" % "2.18.3" % Test,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test,
  "org.powermock" % "powermock-core" % "2.0.0-beta.5" % Test
)

assemblyJarName in assembly := "heimdali-api.jar"

parallelExecution in Test := false

addCompilerPlugin(
  "org.spire-math" % "kind-projector" % "0.9.7" cross CrossVersion.binary
)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")

scalacOptions ++= Seq("-Ypartial-unification", "-language:higherKinds")

scalaVersion in ThisBuild := "2.12.5"

mainClass in reStart := Some("com.heimdali.Main")

Revolver.enableDebugging(port = 9999, suspend = true)

javaOptions in reStart := Seq(
  "-Dhadoop.home.dir=$PWD",
  s"-Djava.security.krb5.conf=${baseDirectory.value}/phdata-conf/krb5.conf"
)

unmanagedClasspath in Runtime += baseDirectory.value / "phdata-conf"

envVars in reStart := Map(
  "HEIMDALI_LDAP_GROUP_PATH" -> "ou=groups,ou=Heimdali,DC=phdata,DC=io",
  "HEIMDALI_SECRET" -> """r8,q8vx~AnU^U"W.""",
  "HEIMDALI_HDFS_DS_ROOT" -> "/data/governed",
  "HEIMDALI_LDAP_PORT" -> "389",
  "HEIMDALI_CM_ADMIN_PASSWORD" -> "!Heimdali14!",
  "HEIMDALI_DB_USER" -> "root",
  "HEIMDALI_DB_PASS" -> "my-secret-pw",
  "HEIMDALI_API_SERVICE_PRINCIPAL" -> "heimdali_api/edge1.valhalla.phdata.io@PHDATA.IO",
  "HEIMDALI_CLUSTER_NAME" -> "cluster",
  "HEIMDALI_CM_URL_BASE" -> "https://manager.valhalla.phdata.io:7183/api/v14",
  "HEIMDALI_UI_HOME" -> "/opt/cloudera/parcels/HEIMDALI-2018.04.61/usr/lib/heimdali-ui",
  "HEIMDALI_API_HOME" -> "/opt/cloudera/parcels/HEIMDALI-2018.04.61/usr/lib/heimdali-api",
  "HEIMDALI_REST_PORT" -> "8080",
  "HEIMDALI_LDAP_BASE_DN" -> "DC=phdata,DC=io",
  "HEIMDALI_HDFS_SW_ROOT" -> "/data/shared_workspaces",
  "HEIMDALI_LDAP_ADMIN_PASS" -> "zp7CuBQLYYdDczBx",
  "HEIMDALI_CM_ADMIN_USER" -> "bthompson",
  "HEIMDALI_DB_DRIVER" -> "com.mysql.cj.jdbc.Driver",
  "HEIMDALI_HDFS_USER_ROOT" -> "/user",
  "HEIMDALI_HIVE_URL" -> "jdbc:hive2://master2.valhalla.phdata.io:10000/default;ssl=true;principal=hive/_HOST@PHDATA.IO;",
  "HEIMDALI_REALM" -> "PHDATA.IO",
  "HEIMDALI_DB_URL" -> "jdbc:mysql://localhost/heimdali",
  "HEIMDALI_LDAP_HOST" -> "ad1.valhalla.phdata.io",
  "HEIMDALI_LDAP_ADMIN_DN" -> "CN=Heimdali Admin,OU=users,OU=Heimdali,DC=phdata,DC=io",
  "HEIMDALI_CLUSTER_ENVIRONMENT" -> "dev",
  "HEIMDALI_KEYTAB_REFRESH" -> "1h",
  "HEIMDALI_HDFS_USER_SIZE" -> "1",
  "HEIMDALI_HDFS_SHARED_SIZE" -> "1",
  "HEIMDALI_HDFS_DATASET_SIZE" -> "1",
  "HEIMDALI_YARN_USER_CORES" -> "1",
  "HEIMDALI_YARN_USER_MEMORY" -> "1",
  "HEIMDALI_YARN_SHARED_CORES" -> "1",
  "HEIMDALI_YARN_SHARED_MEMORY" -> "1",
  "HEIMDALI_YARN_DATASET_CORES" -> "1",
  "HEIMDALI_YARN_DATASET_MEMORY" -> "1",
  "HEIMDALI_YARN_USER_PARENTS" -> "root,user",
  "HEIMDALI_YARN_SHARED_PARENTS" -> "root",
  "HEIMDALI_YARN_DATASET_PARENTS" -> "root",
  "HEIMDALI_INFRA_APPROVERS" -> "cn=edh_admin_full,ou=groups,ou=Hadoop,dc=phdata,dc=io",
  "HEIMDALI_RISK_APPROVERS" -> "cn=edh_admin_full,ou=groups,ou=Hadoop,dc=phdata,dc=io"
)
