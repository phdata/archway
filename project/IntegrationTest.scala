import sbt._
import sbt.Keys.{artifactName, libraryDependencies, resolvers}
import sbt.{Artifact, ModuleID, ScalaVersion}
import sbtassembly.AssemblyKeys.{assemblyJarName, _}
import sbtassembly.{MergeStrategy, PathList}


object IntegrationTest {
  val customResolvers = Seq(
    "Cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos")

  val assemblySettings = Seq(
    assemblyJarName in assembly := "archway-test-dependencies.jar",
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
      case PathList(ps@_*) if ps.last endsWith "package-info.class" => MergeStrategy.first
      case PathList(ps@_*) if ps.last endsWith "javamail.providers" => MergeStrategy.first
      case PathList(ps@_*) if ps.last endsWith "public-suffix-list.txt" => MergeStrategy.first
      case PathList(ps@_*) if ps.last endsWith "application.test.conf" => MergeStrategy.discard
      case PathList(ps@_*) if ps.last endsWith "krb5.conf" => MergeStrategy.discard
      case PathList(ps@_*) if ps.last endsWith "archway.keytab"  => MergeStrategy.discard
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )

  val settings =
     assemblySettings ++ Seq(
       resolvers ++= customResolvers,
       artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
         s"archway-integration-tests.jar"
       },
       libraryDependencies ++= Dependencies.integrationTestDependencies
     )
}
