package io.phdata.services;

import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO}
import io.phdata.AppContext
import io.phdata.models.{DistinguishedName, Manager, MemberRoleRequest}
import io.phdata.provisioning.DefaultProvisioningService
import io.phdata.test.fixtures._
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext

class EmailServiceIntegrationSpec extends FlatSpec {

    it should "Send new member email" in new Context {
      val workspaceId = 130L
      val dn = DistinguishedName("CN=Tony Foerster,OU=users,OU=Hadoop,DC=phdata,DC=io")

      resources.use { emailService =>
        for {
          _ <- emailService.newMemberEmail(workspaceId, MemberRoleRequest(dn, "data", 1, Some(Manager))).value
        } yield()
      }.unsafeRunSync()
    }

    trait Context {
      implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
      implicit def timer = new TestTimer

      val executor: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

      val resources = for {
      ctx <- AppContext.default[IO]()
      } yield {
        val provisioningService = new DefaultProvisioningService[IO](ctx, executor)
        val memberService = new MemberServiceImpl[IO](ctx)

        val workspaceService = new WorkspaceServiceImpl[IO](provisioningService, memberService, ctx)

        new EmailServiceImpl[IO](ctx, workspaceService)
      }
    }
}
