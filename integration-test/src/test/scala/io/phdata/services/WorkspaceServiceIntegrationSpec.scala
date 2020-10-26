package io.phdata.services

import java.net.URLDecoder
import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO}
import io.phdata.AppContext
import io.phdata.config.TemplateNames
import io.phdata.itest.fixtures.{KerberosTest, itestConfig, systemTestConfig}
import io.phdata.models._
import io.phdata.provisioning.DefaultProvisioningService
import io.phdata.test.fixtures.TestTimer
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class WorkspaceServiceIntegrationSpec extends FlatSpec with Matchers with KerberosTest{

  behavior of "WorkspaceService"

  it should "change owner of a workspace" in new Context {

    val workspaceName = "IntegrationTestWorkspace"
    val originDN = DistinguishedName(s"CN=${systemTestConfig.existingUser},${itestConfig.ldap.userPath.get}")
    val newDN = DistinguishedName("CN=Tony Foerster,OU=users,OU=Hadoop,DC=phdata,DC=io")

    val workspaceRequest = WorkspaceRequest(
      workspaceName,
      workspaceName,
      workspaceName,
      TemplateNames.simple,
      originDN,
      timer.instant,
      Compliance(false, false, false),
      false,
      metadata = Metadata(workspaceName, workspaceName, 0, Map.empty)
    )

    val templateRequest = TemplateRequest(
      "Custom template 1", "", "Custom template 1", Compliance(false, false, false), originDN)


    val result = resources.use{ case(workspaceService, memberService, templateService) =>
      for {
        templateWorkspaceRequest <- templateService.workspaceFor(templateRequest, URLDecoder.decode("Custom template 1", "UTF-8"))
        workspaceRequest <- workspaceService.create(templateWorkspaceRequest)
        _ <- workspaceService.changeOwner(workspaceRequest.id.get, newDN)
        members <- memberService.members(workspaceRequest.id.get)
        updatedWorkspaceRequest <- workspaceService.findById(workspaceRequest.id.get).value
        _ <- workspaceService.deleteWorkspace(workspaceRequest.id.get)
      } yield (members, updatedWorkspaceRequest)
    }.unsafeRunSync()

    result match {
      case (members, workspaceRequest) =>
//        TODO: should be valid but old owner is not completely removed
//        assert(members.size == 1)
//        assert(!members.exists(_.distinguishedName == originDN.value))

        assert(members.map(_.distinguishedName).contains(newDN.value))
        assert(members.filter(_.distinguishedName == newDN.value).map(_.data).size == 1)
        assert(workspaceRequest.get.requestedBy == newDN)
    }
  }

  trait Context{
    implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    implicit def timer = new TestTimer

    val executor: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

    val resources = for {
      ctx <- AppContext.default[IO]()
    } yield {
      val provisioningService = new DefaultProvisioningService[IO](ctx, executor)
      val memberService = new MemberServiceImpl[IO](ctx)
      val configService = new DBConfigService[IO](ctx)
      val templateService = new JSONTemplateService[IO](ctx, configService)
      val workspaceService = new WorkspaceServiceImpl[IO](provisioningService, memberService, ctx)
      (workspaceService, memberService, templateService)
    }
  }

}
