package io.phdata.services

import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO}
import io.phdata.AppContext
import io.phdata.itest.fixtures.KerberosTest
import io.phdata.test.fixtures.TestTimer
import doobie.implicits._
import io.phdata.models.DistinguishedName
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext

class ImpalaServiceIntegrationSpec extends FlatSpec with KerberosTest {

  val requester = DistinguishedName("CN=Tony Foerster,OU=users,OU=Hadoop,DC=phdata,DC=io")

  it should "Invalidate metadata" in new Context {
    context
      .use {
        context =>
          for {
            workspaceId <- context.workspaceRequestRepository.list(requester).transact(context.transactor)
            _ <- ImpalaService.invalidateMetadata(workspaceId.head.id)(context)
          } yield ()
      }
      .unsafeRunSync()
  }

  trait Context {
    implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

    implicit def timer = new TestTimer

    val provisionEC = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))

    val context = for {
      context <- AppContext.default[IO]()

    } yield context

  }

}
