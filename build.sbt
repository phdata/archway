lazy val models = (project in file("models"))
  .settings(Common.settings: _*)
  .settings(Models.modelsSettings: _*)

lazy val common = (project in file("common"))
  .settings(Common.settings: _*)
  .settings(Common.commonSettings: _*)
  .dependsOn(models)

lazy val provisioning = (project in file("provisioning"))
  .settings(Common.settings: _*)
  .settings(Provisioning.provisioningSettings: _*)
  .dependsOn(
    models, 
    common,
    `integration-test` % "test->test"
  )

lazy val api = (project in file("api"))
  .settings(Common.settings: _*)
  .settings(API.apiSettings: _*)
  .dependsOn(
    models,
    common,
    provisioning,
    `integration-test` % "test->test"
  )

lazy val templates = (project in file("templates"))
  .settings(Common.settings: _*)
  .dependsOn(
    models,
    common,
  )

lazy val `integration-test` = (project in file("integration-test"))
  .settings(IntegrationTest.settings: _*)
  .dependsOn(
    common,
    common % "test->test",
    models
  )
