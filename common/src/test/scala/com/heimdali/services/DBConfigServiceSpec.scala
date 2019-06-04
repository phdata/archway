package com.heimdali.services

import cats.effect._
import cats.implicits._
import com.heimdali.AppContext
import com.heimdali.test.fixtures._
import doobie._
import doobie.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext

class DBConfigServiceSpec extends FlatSpec with MockFactory with AppContextProvider {

  behavior of "DBConfigService"

  it should "get and set a gid" in new Context {

    context.configRepository.getValue _ expects "nextgid" returning "123".pure[ConnectionIO]
    context.configRepository.setValue _ expects("nextgid", "124") returning ().pure[ConnectionIO]

    configService.getAndSetNextGid.unsafeRunSync()

  }

  implicit val contextShift = IO.contextShift(ExecutionContext.global)

  trait Context {

    val context: AppContext[IO] = genMockContext()

    val configService = new DBConfigService[IO](context)

  }

}
