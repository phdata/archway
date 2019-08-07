package io.phdata.startup

import java.util.concurrent.Executors

import cats.effect._
import io.phdata.AppContext
import io.phdata.services.ProvisioningService
import io.phdata.test.fixtures.{AppContextProvider, _}
import io.phdata.AppContext
import io.phdata.services.ProvisioningService
import io.phdata.test.fixtures.AppContextProvider
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class ArchwayStartupSpec extends FlatSpec with Matchers with MockFactory with AppContextProvider {

  it should "run two startup jobs" in new Context {
    (context.loginContextProvider.kinit[IO]()(_: Sync[IO])).expects(*).returning(timer.sleep(100 millis)).atLeastTwice()
    (provisioningService.provisionAll _).expects().returning(timer.sleep(100 millis)).atLeastTwice()

    (for {
      fibers <- startup.begin()
      _ <- timer.sleep(500 millis)
      _ <- fibers.traverse[IO, Unit](_.cancel)
    } yield ()).unsafeRunSync()
  }

  trait Context {

    val executor: ExecutionContext =
      ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
    implicit val timer: Timer[IO] =
      IO.timer(executor)
    implicit val contextShift: ContextShift[IO] =
      IO.contextShift(executor)

    val provisioningService: ProvisioningService[IO] =
      mock[ProvisioningService[IO]]

    val context: AppContext[IO] = genMockContext(
      appConfig = appConfig.copy(
        provisioning = appConfig.provisioning.copy(provisionInterval = 100 millis),
        cluster = appConfig.cluster.copy(sessionRefresh = 100 millis)))

    val provisioningJob: Provisioning[IO] =
      new Provisioning[IO](context, provisioningService)
    val sessionMaintainer: SessionMaintainer[IO] =
      new SessionMaintainer[IO](context)
    lazy val startup: ArchwayStartup[IO] = new ArchwayStartup[IO](provisioningJob, sessionMaintainer)(executor)
  }

}
