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
    assemblyJarName in assembly := "archway-server.jar",
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
