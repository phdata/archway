/* Copyright 2018 phData Inc. */

package com.heimdali.startup

import java.util.concurrent.Executors

import cats.effect._
import com.heimdali.services.{LoginContextProvider, ProvisioningService}
import com.heimdali.test.fixtures.{TestTimer, _}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class StartupSpec extends FlatSpec with MockFactory with Matchers {

  it should "run two startup jobs" in new Context {
    (loginContextProvider.kinit[IO]()(_: Sync[IO])).expects(*).returning(IO.unit).atLeastTwice()
    (provisioningService.provisionAll _).expects().returning(IO.unit).atLeastTwice()

    startup.begin().unsafeToFuture()

    Thread.sleep(500)
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
      new Provisioning[IO](appConfig.provisioning, provisioningService)
    val sessionMaintainer: SessionMaintainer[IO] =
      new SessionMaintainer[IO](appConfig.cluster, loginContextProvider)
    val startup: HeimdaliStartup[IO] =
      new HeimdaliStartup[IO](provisioningJob, sessionMaintainer)(executor)
  }

}
