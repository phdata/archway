lazy val IntegrationTest = config("it") extend (Test)

lazy val models = (project in file("models"))
  .settings(Common.settings: _*)
  .settings(Models.modelsSettings: _*)

lazy val common = (project in file("common"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .settings(Common.settings: _*)
  .settings(Common.commonSettings: _*)
  .dependsOn(models)

lazy val provisioning = (project in file("provisioning"))
  .settings(Common.settings: _*)
  .settings(Provisioning.provisioningSettings: _*)
  .dependsOn(
    models, 
    common,
    common % "test->it"
  )

lazy val api = (project in file("api"))
  .settings(Common.settings: _*)
  .settings(API.apiSettings: _*)
  .dependsOn(
    models,
    common,
    provisioning,
    common % "test->it"
  )

lazy val templates = (project in file("templates"))
  .settings(Common.settings: _*)
  .dependsOn(
    models,
    common,
  )

lazy val `system-tests` = (project in file("system-tests"))
  .settings(SystemTests.settings: _*)
  .dependsOn(
    common,
    models
  )
