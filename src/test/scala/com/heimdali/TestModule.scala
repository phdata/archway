package com.heimdali

import org.scalatestplus.play.FakeApplicationFactory
import play.api.inject.{Binding, Module}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration, Environment}

class TestModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(bind[TestApplicationFactory].toSelf.eagerly())
  }
}

trait TestApplicationFactory extends FakeApplicationFactory {
  def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .configure(Map("heimdali.database.url" -> "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"))
      .build()
  }
}