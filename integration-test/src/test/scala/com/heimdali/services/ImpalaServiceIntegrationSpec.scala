package com.heimdali.services

import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO, Timer}
import com.heimdali.AppContext
import com.heimdali.itest.fixtures.KerberosTest
import com.heimdali.test.fixtures.TestTimer
import org.scalatest.FlatSpec
import doobie.implicits._

import scala.concurrent.ExecutionContext

class ImpalaServiceIntegrationSpec extends FlatSpec with KerberosTest {

  val requester = "CN=Tony Foerster,OU=users,OU=Hadoop,DC=phdata,DC=io"

  it should "Invalidate metadata" in new Context {
    context
      .use {
        case context =>
          for {
            workspaceId <- context.workspaceRequestRepository.list(requester).transact(context.transactor)
            _ <- ImpalaService.invalidateMetadata(workspaceId.head.id, context)
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

    } yield (context)

  }

}
