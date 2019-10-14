package io.phdata.services

import java.time.Instant

import cats.effect.{Clock, IO}
import io.phdata.AppContext
import io.phdata.test.fixtures.{AppContextProvider}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import io.phdata.test.fixtures._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import cats.implicits._

class YarnServiceSpec extends FlatSpec with Matchers with MockFactory with AppContextProvider{

  it should "modify number of cores and memories" in new Context {
    inSequence {
        context.yarnClient.setupPool _ expects(poolName, maxCores, maxMemoryInGB) returning ().pure[IO]
        context.yarnRepository.update _ expects(initialYarn, id, now) returning 1.pure[ConnectionIO]
      }

    yarnService.updateYarnResources(initialYarn, id, now)
  }

  trait Context{
    val now = Instant.now()
    implicit val clock: Clock[IO] = testTimer.clock
    val context: AppContext[IO] = genMockContext()

    lazy val yarnService = new YarnServiceImpl[IO](context)
  }
}