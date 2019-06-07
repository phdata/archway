import sbt._
import sbt.Keys.{artifactName, libraryDependencies, resolvers}
import sbt.{Artifact, ModuleID, ScalaVersion}
import sbtassembly.AssemblyKeys.{assemblyJarName, _}
import sbtassembly.{MergeStrategy, PathList}


object IntegrationTest {
  val customResolvers = Seq(
    "Cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos")

  val assemblySettings = Seq(
    assemblyJarName in assembly := "heimdali-test-dependencies.jar",
    assemblyMergeStrategy in assembly := {
      case PathList(ps@_*) if ps.last endsWith "package-info.class" => MergeStrategy.first
      case PathList(ps@_*) if ps.last endsWith "javamail.providers" => MergeStrategy.first
      case PathList(ps@_*) if ps.last endsWith "public-suffix-list.txt" => MergeStrategy.first
      case PathList(ps@_*) if ps.last endsWith "application.test.conf" => MergeStrategy.discard
      case PathList(ps@_*) if ps.last endsWith "krb5.conf" => MergeStrategy.discard
      case PathList(ps@_*) if ps.last endsWith "heimdali.keytab"  => MergeStrategy.discard
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )

  val settings =
     assemblySettings ++ Seq(
       resolvers ++= customResolvers,
       artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
         s"heimdali-integration-tests.jar"
       },
       libraryDependencies ++= Dependencies.integrationTestDependencies
     )
}
