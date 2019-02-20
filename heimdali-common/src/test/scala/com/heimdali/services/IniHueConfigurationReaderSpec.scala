package com.heimdali.services

import cats.effect.IO
import org.scalatest.{FlatSpec, Matchers}

class IniHueConfigurationReaderSpec extends FlatSpec with Matchers {

  behavior of "IniHueConfigurationReaderSpec"

  it should "getValue" in {
    val expected = "8080"

    val hueConfigurationReader = new IniHueConfigurationReader[IO]()

    val Some(actual) = hueConfigurationReader.getValue("desktop.http_port").value.unsafeRunSync()

    actual shouldBe expected
  }

}
