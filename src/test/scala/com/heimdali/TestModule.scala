package com.heimdali

import org.scalatestplus.play.FakeApplicationFactory
import play.api.inject.Binding
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration, Environment}
import play.api.test.Helpers._

class TestModule extends HeimdaliModule {}

trait TestApplicationFactory extends FakeApplicationFactory with LDAPTest {
  val ldapPort = 1234

  val ldapConfig = Map(
    "server" -> "localhost",
    "port" -> ldapPort,
    "connections" -> 1,
    "base-dn" -> "dc=jotunn,dc=io",
    "users-path" -> "ou=marketing"
  )

  val dbConfig = Map(
    "dbs" -> Map(
      "default" -> Map(
        "driver" -> "slick.driver.H2Driver$",
        "db" -> Map(
          "driver" -> "org.h2.Driver",
          "url" -> "jdbc:h2:mem:play"
        )
      )
    )
  )

  def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .configure("ldap" -> ldapConfig)
      .configure(inMemoryDatabase(""))
      .build()
  }
}