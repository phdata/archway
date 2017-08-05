name := "heimdali-api"

version := "2018.08.01"

scalaVersion := "2.12.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  ehcache,
  guice,
  "be.objectify" %% "deadbolt-scala" % "2.6.0",
  "com.pauldijou" %% "jwt-play-json" % "0.14.0",
  "com.unboundid" % "unboundid-ldapsdk" % "4.0.0",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test)

unmanagedSourceDirectories in Compile += baseDirectory.value / "src" / "main" / "scala"

unmanagedResourceDirectories in Compile += baseDirectory.value / "src" / "main" / "resources"

unmanagedSourceDirectories in Test += baseDirectory.value / "src" / "test" / "scala"

unmanagedResourceDirectories in Test += baseDirectory.value / "src" / "test" / "resources"

unmanagedClasspath in Compile ++= (unmanagedResources in Compile).value