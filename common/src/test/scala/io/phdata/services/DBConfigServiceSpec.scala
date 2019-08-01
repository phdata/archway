package io.phdata.services

import cats.effect._
import cats.implicits._
import io.phdata.AppContext
import io.phdata.test.fixtures._
import doobie._
import doobie.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

class DBConfigServiceSpec extends FlatSpec with MockFactory with AppContextProvider {

  behavior of "DBConfigService"

  it should "get and set a gid" in new Context {

    context.configRepository.getValue _ expects "nextgid" returning "123".pure[ConnectionIO]
    context.configRepository.setValue _ expects("nextgid", "124") returning ().pure[ConnectionIO]

    configService.getAndSetNextGid.unsafeRunSync()

  }

  trait Context {

    val context: AppContext[IO] = genMockContext()

    val configService = new DBConfigService[IO](context)

  }

}
