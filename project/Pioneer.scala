import sbt._
import sbt.Keys._

object Pioneer {

  val settings = Seq(
    artifactName := { (_: ScalaVersion, _: ModuleID, _: Artifact) =>
      "custom-pioneer.jar"
    }
  )

}
