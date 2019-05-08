package com.heimdali.startup
import java.util.concurrent.Executors

import cats.effect._
import cats.implicits._
import com.heimdali.services.{LoginContextProvider, ProvisioningService}
import com.heimdali.test.fixtures.{TestTimer, _}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class HeimdaliStartupSpec extends FlatSpec with Matchers with MockFactory {

  it should "run two startup jobs" in new Context {
    (loginContextProvider.kinit[IO]()(_: Sync[IO])).expects(*).returning(IO.unit).atLeastTwice()
    (provisioningService.provisionAll _).expects().returning(IO.unit).atLeastTwice()

    startup
      .begin()
      .map(_ => Thread.sleep((5 seconds).toMillis).pure[IO])
      .unsafeRunSync()
  }

  trait Context {
    implicit val contextShift: ContextShift[IO] =
      IO.contextShift(ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1)))
    implicit val timer: Timer[IO] =
      new TestTimer
    val executor: ExecutionContext =
      ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

    val loginContextProvider: LoginContextProvider =
      mock[LoginContextProvider]
    val provisioningService: ProvisioningService[IO] =
      mock[ProvisioningService[IO]]

    val provisioningJob: Provisioning[IO] =
      new Provisioning[IO](appConfig.provisioning.copy(provisionInterval = 500 milliseconds), provisioningService)
    val sessionMaintainer: SessionMaintainer[IO] =
      new SessionMaintainer[IO](appConfig.cluster.copy(sessionRefresh = 500 milliseconds), loginContextProvider)
    lazy val startup: HeimdaliStartup[IO] =
      new HeimdaliStartup[IO](provisioningJob, sessionMaintainer)(executor)
  }

}