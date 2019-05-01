package com.heimdali.test.fixtures

import cats.effect._
import cats.implicits._
import com.heimdali.services.ConfigService

import scala.io.Source

class TestConfigService extends ConfigService[IO] {
  override def getAndSetNextGid: IO[Long] =
    123L.pure[IO]

  override def getTemplate(templateName: String): IO[String] =
    IO(Source.fromFile(getClass.getResource("/json.ssp").toURI).getLines().mkString)
}