lazy val common = (project in file("common"))
  .settings(Common.settings: _*)
  .settings(Common.commonSettings: _*)

lazy val provisioning = (project in file("provisioning"))
  .settings(Common.settings: _*)
  .settings(Provisioning.provisioningSettings: _*)
  .dependsOn(
    common,
    common % "test->test"
  )

lazy val api = (project in file("api"))
  .settings(Common.settings: _*)
  .settings(API.apiSettings: _*)
  .dependsOn(
    common,
    common % "test->test",
    provisioning
  )

lazy val templates = (project in file("templates"))
  .settings(Common.settings: _*)
  .dependsOn(
    common
  )

lazy val `integration-test` = (project in file("integration-test"))
  .settings(IntegrationTest.settings: _*)
  .dependsOn(
    api,
    provisioning,
    common % "test->test"
  )
