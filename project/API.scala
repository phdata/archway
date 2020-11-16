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
    assemblyMergeStrategy in assembly := {
      case PathList("javax", "servlet", xs @ _*) => MergeStrategy.first
      case PathList("javax", "xml", xs @ _*) => MergeStrategy.first
      case PathList("javax", "activation", xs @ _*) => MergeStrategy.first
      case PathList("javax", "el", xs @ _*) => MergeStrategy.first
      case PathList("javax", "jdo", xs @ _*) => MergeStrategy.first
      case PathList("org", "datanucleus", xs @ _*) => MergeStrategy.first
      case PathList("com", "zaxxer", xs @ _*) => MergeStrategy.first
      case PathList("org", "apache", "jasper", xs @ _*) => MergeStrategy.first
      case PathList("org", "hamcrest", xs @ _*) => MergeStrategy.first
      case PathList("META-INF", "maven", xs @ _*) => MergeStrategy.discard
      case PathList("META-INF", "native", xs @ _*) => MergeStrategy.discard
      case PathList(ps@_*) if ps.last.endsWith("module-info.class") => MergeStrategy.discard
      case PathList(ps@_*) if ps.last.endsWith("git.properties") => MergeStrategy.discard
      case PathList(ps@_*) if ps.last.endsWith("-site.xml") => MergeStrategy.discard
      case PathList(ps@_*) if ps.last.endsWith("plugin.xml") => MergeStrategy.discard
      case PathList(ps@_*) if ps.last.endsWith(".dat") => MergeStrategy.discard
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
