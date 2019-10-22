package io.phdata.services

import java.time.Instant

import cats.effect.{Clock, IO}
import io.phdata.AppContext
import io.phdata.clients.HDFSAllocation
import io.phdata.test.fixtures.AppContextProvider
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import io.phdata.test.fixtures._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import cats.implicits._

class HDFSServiceSpec extends FlatSpec with Matchers with MockFactory with AppContextProvider{

  it should "set a quota" in new Context {
    inSequence {
      context.hdfsClient.setQuota _ expects(hdfsPathString, hdfsRequestedSize) returning hdfsAllocation.pure[IO]
      context.databaseRepository.quotaSet _ expects(id, now) returning 1.pure[ConnectionIO]
    }

    hdfsService.setQuota(hdfsPathString, hdfsRequestedSize, id, now)
  }

  it should "remove a quota" in new Context {
    inSequence {
      context.hdfsClient.removeQuota _ expects(hdfsPathString) returning ().pure[IO]
    }

    hdfsService.removeQuota(hdfsPathString)
  }

  trait Context{
    val hdfsPathString = "hdfs://cluster/data/governed/staging/sesame_test_1"
    val hdfsAllocation = HDFSAllocation(hdfsPathString, hdfsRequestedSize)
    val now = Instant.now()

    implicit val clock: Clock[IO] = testTimer.clock
    val context: AppContext[IO] = genMockContext()

    lazy val hdfsService = new HDFSServiceImpl[IO](context.hdfsClient, context.databaseRepository, context.transactor)
  }
}
