import Dependencies._

name := "heimdali-api"

version := "2018.08.01"

scalaVersion := "2.12.5"

resolvers += "Cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos"

resolvers += "Apache" at "http://repo.spring.io/plugins-release/"

fullResolvers := ("Jboss" at "https://repository.jboss.org/maven2") +: resolvers.value

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

libraryDependencies ++=
  coreTest ++ dbCore ++ logging ++ bouncy ++ pureConfig ++ iniConfig ++
    http4s ++ fs2 ++ mailer ++ doobie ++ cats ++ catsEffect ++ circe ++ jwt ++ unbound ++ hadoop ++ fs2Http ++ scalatags

assemblyMergeStrategy in assembly := {
  case PathList(ps@_*) if ps.last endsWith "-site.xml" => MergeStrategy.discard
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

  "HEIMDALI_UI_URL" -> "http://master1.jotunn.io:8181",
  "HEIMDALI_SMTP_HOST" -> "smtp.gmail.com",
  "HEIMDALI_SMTP_PORT" -> "587",
  "HEIMDALI_SMTP_SSL" -> "true",
  "HEIMDALI_SMTP_SHOULD_AUTH" -> "true",
  "HEIMDALI_SMTP_USER" -> "username@gmail.com",
  "HEIMDALI_SMTP_PASS" -> "supersecret",


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
