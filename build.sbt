lazy val root = (project in file("."))
  .aggregate(api, common, provisioning, api, pioneer)

lazy val models = (project in file("models"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(Common.settings: _*)
  .settings(Models.modelsSettings: _*)

lazy val common = (project in file("common"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(Common.settings: _*)
  .settings(Common.commonSettings: _*)
  .dependsOn(models)

lazy val provisioning = (project in file("provisioning"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(Common.settings: _*)
  .settings(Provisioning.provisioningSettings: _*)
  .dependsOn(models, common % "compile->compile;test->test;it->it")

lazy val api = (project in file("api"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(Common.settings: _*)
  .settings(API.apiSettings: _*)
  .dependsOn(
    models,
    common % "compile->compile;test->test;it->it",
    provisioning % "compile->compile;test->test;it->it"
  )

lazy val pioneer = (project in file("custom-pioneer"))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(Common.settings: _*)
  .settings(Pioneer.settings: _*)
  .dependsOn(common)