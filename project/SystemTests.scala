import sbt.Keys.libraryDependencies
import sbtassembly.AssemblyKeys.{assemblyJarName, _}


object SystemTests {

  val assemblySettings = Seq(
    assemblyJarName in assembly := "system-tests.jar",
  )

  val settings =
    Seq(libraryDependencies ++= Dependencies.systemTestsDependencies) ++ assemblySettings
}
