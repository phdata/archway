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

lazy val `provisioning-app` = (project in file("provisioning-app"))
  .settings(Common.settings: _*)
  .settings(Provisioning.provisioningSettings: _*)
  .dependsOn(
    provisioning
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

lazy val pioneer = (project in file("custom-pioneer"))
  .settings(Common.settings: _*)
  .settings(Pioneer.settings: _*)
  .dependsOn(common)