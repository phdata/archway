import sbt._
import sbt.Keys._
import sbtassembly.AssemblyKeys._
import sbtassembly._
import spray.revolver.RevolverPlugin.autoImport._

object API {

  val testSettings = Seq(
    parallelExecution in Test := false,
    unmanagedClasspath in Test ++= Seq(
      baseDirectory.value / "sentry-conf",
      baseDirectory.value / "hive-conf"
    )
  )

  val assemblySettings = Seq(
    assemblyJarName in assembly := "heimdali-api.jar",
    assemblyExcludedJars in assembly := {
      val cp = (fullClasspath in assembly).value
      cp filter {_.data.getPath.contains("bouncycastle")}
    },
    assemblyMergeStrategy in assembly := {
      case PathList(ps@_*) if ps.last.endsWith("-site.xml") => MergeStrategy.discard
      case PathList(ps@_*) if ps.last.endsWith("public-suffix-list.txt") => MergeStrategy.first
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )

  val revolverSettings = Seq(
    mainClass in reStart := Some("io.phdata.Server"),
    javaOptions in reStart := Seq(
      "-Dhadoop.home.dir=$PWD",
      s"-Djava.security.krb5.conf=${baseDirectory.value}/../krb5.conf"
    ),
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

      "HEIMDALI_LDAP_HOST" -> "165.227.59.134",
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
      "HEIMDALI_DB_PASS" -> "Jotunn123!",
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
  )

  val projectorSettings = Seq(
    resolvers += Resolver.sonatypeRepo("releases"),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9")
  )

  val apiSettings =
    Seq(libraryDependencies ++= Dependencies.apiDependencies) ++
      projectorSettings ++ assemblySettings ++ revolverSettings ++ testSettings

}
