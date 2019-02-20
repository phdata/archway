import sbt.Keys._

object Provisioning {
  
  val provisioningSettings =
    Seq(libraryDependencies ++= Dependencies.provisioningDependencies)
  
}
