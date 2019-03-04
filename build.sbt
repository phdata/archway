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
  .dependsOn(models, common % "compile->compile;test->test")

lazy val api = (project in file("api"))
  .settings(Common.settings: _*)
  .settings(API.apiSettings: _*)
  .dependsOn(
    models,
    common % "compile->compile;test->test",
    provisioning % "compile->compile;test->test"
  )

lazy val pioneer = (project in file("custom-pioneer"))
  .settings(Common.settings: _*)
  .dependsOn(common)