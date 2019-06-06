import sbt._
import sbt.Keys._

object Provisioning {
  
  val provisioningSettings =
    Seq(
      addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
      libraryDependencies ++= Dependencies.provisioningDependencies
    )
  
}
