import sbt.Keys.libraryDependencies

object ProvisioningApp {
  val provisioningAppSettings =
    Seq(libraryDependencies ++= Dependencies.provisioningAppDependencies)
}
