name := "heimdali-api"

version := "2018.08.01"

scalaVersion := "2.12.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  guice,
  ws,
  "org.julienrf" %% "play-json-derived-codecs" % "4.0.0",
  "be.objectify" %% "deadbolt-scala" % "2.6.0" withSources(),
  "com.pauldijou" %% "jwt-play-json" % "0.14.0",
  "com.typesafe.play" %% "play-json-joda" % "2.6.0",
  "com.unboundid" % "unboundid-ldapsdk" % "4.0.0",
  "io.getquill" %% "quill" % "1.4.0",
  "org.postgresql" % "postgresql" % "42.1.4",
  "org.flywaydb" % "flyway-core" % "4.2.0",
  "org.apache.hadoop" % "hadoop-client" % "2.6.0" % "provided" excludeAll(ExclusionRule(organization = "org.slf4j"),
                                                                          ExclusionRule(organization = "com.google.guava")),
  "com.novocode" % "junit-interface" % "0.11" % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.5.3" % Test,
  "org.mockito" % "mockito-core" % "2.+" % Test,
  "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test)

libraryDependencies ++= Seq(
  "org.apache.hadoop" % "hadoop-hdfs" % "2.6.0" % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-common" % "2.6.0" % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-client" % "2.6.0" % Test classifier "" classifier "tests",
  "org.apache.hadoop" % "hadoop-minicluster" % "2.6.0" % Test
).map(_.excludeAll(ExclusionRule(organization = "javax.servlet"),
  ExclusionRule(organization = "com.google.guava")))

dependencyOverrides += "com.google.guava" % "guava" % "19.0"

unmanagedSourceDirectories in Compile += baseDirectory.value / "src" / "main" / "scala"

unmanagedResourceDirectories in Compile += baseDirectory.value / "src" / "main" / "resources"

unmanagedSourceDirectories in Test += baseDirectory.value / "src" / "test" / "scala"

unmanagedResourceDirectories in Test += baseDirectory.value / "src" / "test" / "resources"

unmanagedClasspath in Compile ++= (unmanagedResources in Compile).value

unmanagedClasspath in Test ++= (unmanagedResources in Test).value

packageName in Universal := "heimdali-api"

parallelExecution in Test := false
