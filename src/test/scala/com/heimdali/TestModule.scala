package com.heimdali

import org.scalatestplus.play.FakeApplicationFactory
import play.api.inject.Binding
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration, Environment}

class TestModule extends HeimdaliModule {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq()
  }
}

trait TestApplicationFactory extends FakeApplicationFactory with LDAPTest {
  val ldapPort = 1234

  val ldapConfig = Map(
    "server" -> "localhost",
    "port" -> ldapPort,
    "connections" -> 1,
    "base-dn" -> "dc=jotunn,dc=io",
    "users-path" -> "ou=marketing"
  )

  def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      //      .load(new TestModule)
      .configure("ldap" -> ldapConfig)
      .build()
  }
}