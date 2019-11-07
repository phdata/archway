val customResolvers = Seq(
  "Cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos",
  "Apache" at "http://repo.spring.io/plugins-release/",
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

val compilerOptions = Seq(
  "-Ypartial-unification",
  "-language:higherKinds",
  "-deprecation"
)

lazy val ItTest = config("it") extend (Test)

def itFilter(name: String): Boolean   = name endsWith "IntegrationSpec"
def unitFilter(name: String): Boolean = (name endsWith "Spec") && !itFilter(name)

val testSettings = Seq(
  testOptions in ItTest := Seq(Tests.Filter(itFilter)),
  testOptions in Test := Seq(Tests.Filter(unitFilter)),
)

val settings = Seq(
  scalaVersion := "2.12.5",
  organization := "io.phdata",
  version := "2018.08.01",
  resolvers ++= customResolvers,
  scalacOptions := compilerOptions,
  run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run)).evaluated,
  runMain in Compile := Defaults.runMainTask(fullClasspath in Compile, runner in(Compile, run)).evaluated,
  unmanagedJars in Compile ++= Seq(file("lib/ImpalaJDBC41-no-log4j.jar"))
) ++ testSettings        

lazy val common = (project in file("common"))
  .settings(settings: _*)
  .settings(Common.commonSettings: _*)

lazy val provisioning = (project in file("provisioning"))
  .settings(settings: _*)
  .settings(Provisioning.provisioningSettings: _*)
  .dependsOn(
    common,
    common % "test->test"
  )

lazy val api = (project in file("api"))
  .settings(settings: _*)
  .settings(API.apiSettings: _*)
  .dependsOn(
    common,
    common % "test->test",
    provisioning
  )

lazy val templates = (project in file("templates"))
  .settings(settings: _*)
  .dependsOn(
    common
  )

lazy val `integration-test` = (project in file("integration-test"))
  .configs(ItTest)
  .settings(IntegrationTest.settings: _*)
  .settings(testSettings: _*)
  .settings(
    inConfig(ItTest)(Defaults.testTasks)
  )
  .dependsOn(
    api,
    provisioning,
    common % "test->test"
  )
