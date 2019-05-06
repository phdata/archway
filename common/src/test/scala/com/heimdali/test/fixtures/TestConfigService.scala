package com.heimdali.test.fixtures

import cats.effect._
import cats.implicits._
import com.heimdali.services.ConfigService

class TestConfigService extends ConfigService[IO] {

  override def getAndSetNextGid: IO[Long] =
    123L.pure[IO]

}