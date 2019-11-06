import sbt._
import sbt.Keys._
import org.fusesource.scalate.ScalatePlugin._
import ScalateKeys._

object Common {

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

  val commonSettings = scalateOptions ++ Seq(
    artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
      s"archway-common-tests.jar"
    },
    libraryDependencies ++= Dependencies.commonDependencies
  )

}
