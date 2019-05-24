import sbt._
import sbt.Keys._
import org.fusesource.scalate.ScalatePlugin._
import ScalateKeys._

object Common {

  val customResolvers = Seq(
    "Cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos",
    "Apache" at "http://repo.spring.io/plugins-release/",
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )

  val scalateOptions = scalateSettings ++ Seq(
    scalateOverwrite := true,
    scalateTemplateConfig in Compile := Seq(
      TemplateConfig(
        baseDirectory.value / "src" / "main" / "templates",
        Nil,
        Nil,
        Some("templates")
      )
    )
  )

  val jossResolver = "Jboss" at "https://repository.jboss.org/maven2"

  val compilerOptions = Seq(
    "-Ypartial-unification",
    "-language:higherKinds"
  )

  val settings = Seq(
    scalaVersion := "2.12.5",
    organization := "io.phdata",
    version := "2018.08.01",
    resolvers ++= customResolvers,
    scalacOptions := compilerOptions,
    run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run)).evaluated,
    runMain in Compile := Defaults.runMainTask(fullClasspath in Compile, runner in(Compile, run)).evaluated
  )

  val commonSettings = scalateOptions ++ Seq(
    libraryDependencies ++= Dependencies.commonDependencies
  )

}
