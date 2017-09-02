name := "heimdali-api"

version := "2018.08.01"

scalaVersion := "2.12.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  ehcache,
  guice,
  ws,
  jdbc % Test,
  "org.julienrf" %% "play-json-derived-codecs" % "4.0.0",
  "be.objectify" %% "deadbolt-scala" % "2.6.0" withSources(),
  "com.pauldijou" %% "jwt-play-json" % "0.14.0",
  "com.typesafe.play" %% "play-json-joda" % "2.6.0",
  "com.unboundid" % "unboundid-ldapsdk" % "4.0.0",
  "com.typesafe.play" %% "play-slick" % "3.0.0",
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.3.0",
  "org.postgresql" % "postgresql" % "42.1.4",
  "org.mockito" % "mockito-core" % "2.+" % Test,
  "com.h2database" % "h2" % "1.4.196" % Test,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test)

unmanagedSourceDirectories in Compile += baseDirectory.value / "src" / "main" / "scala"

unmanagedResourceDirectories in Compile += baseDirectory.value / "src" / "main" / "resources"

unmanagedSourceDirectories in Test += baseDirectory.value / "src" / "test" / "scala"

unmanagedResourceDirectories in Test += baseDirectory.value / "src" / "test" / "resources"

unmanagedClasspath in Compile ++= (unmanagedResources in Compile).value

packageName in Universal := "heimdali-api"
