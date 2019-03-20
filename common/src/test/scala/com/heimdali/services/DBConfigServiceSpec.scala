package com.heimdali.services

import cats.effect._
import cats.implicits._
import com.heimdali.repositories.ConfigRepository
import com.heimdali.test.fixtures._
import doobie._
import doobie.implicits._
import doobie.util.transactor.Strategy
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext

class DBConfigServiceSpec extends FlatSpec with MockFactory {

  behavior of "DBConfigService"

  it should "get and set a gid" in new Context {

    configRepo.getValue _ expects "nextgid" returning "123".pure[ConnectionIO]
    configRepo.setValue _ expects("nextgid", "124") returning ().pure[ConnectionIO]

    configService.getAndSetNextGid.unsafeRunSync()

  }

  trait Context {

    val configRepo = mock[ConfigRepository]
    implicit val contextShift = IO.contextShift(ExecutionContext.global)
    val transactor = Transactor.fromConnection[IO](null, ExecutionContext.global).copy(strategy0 = Strategy(FC.unit, FC.unit, FC.unit, FC.unit))
    val configService = new DBConfigService[IO](appConfig, configRepo, transactor)

  }

}
