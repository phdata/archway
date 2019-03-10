package com.heimdali.services

import cats.effect._
import cats.implicits._
import com.heimdali.repositories.ConfigRepository
import com.heimdali.test.fixtures._
import doobie._
import doobie.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

class DBConfigServiceSpec extends FlatSpec with MockFactory with DBTest {

  behavior of "DBConfigService"

  it should "get and set a gid" in new Context {

    configRepo.getValue _ expects "nextgid" returning "123".pure[ConnectionIO]
    configRepo.setValue _ expects("nextgid", "124") returning ().pure[ConnectionIO]

    configService.getAndSetNextGid.unsafeRunSync()

  }

  trait Context {

    val configRepo = mock[ConfigRepository]
    val configService = new DBConfigService[IO](appConfig, configRepo, transactor)

  }

}
