import sbt._
import sbt.Keys._
import Dependencies._

object Common {

  val customResolvers = Seq(
    "Cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos",
    "Apache" at "http://repo.spring.io/plugins-release/",
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
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
    scalacOptions := compilerOptions
  )

  val commonSettings = Seq(
    libraryDependencies ++= Dependencies.commonDependencies
  )

}
