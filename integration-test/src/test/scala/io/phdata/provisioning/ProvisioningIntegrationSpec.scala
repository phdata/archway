package io.phdata.provisioning

import java.util.concurrent.Executors

import cats.data.NonEmptyList
import cats.effect.{ContextShift, IO, Resource, Timer}
import cats.implicits._
import io.phdata.AppContext
import io.phdata.caching.CacheEntry
import io.phdata.config.TemplateNames
import io.phdata.itest.fixtures.{KerberosTest, SSLTest}
import io.phdata.models.{Compliance, DistinguishedName, TemplateRequest}
import io.phdata.services.{DBConfigService, JSONTemplateService, MemberServiceImpl, WorkspaceServiceImpl}
import io.phdata.test.fixtures.TestTimer
import org.scalatest.{BeforeAndAfterEach, FlatSpec}

import scala.concurrent.ExecutionContext

class ProvisioningIntegrationSpec extends FlatSpec with KerberosTest with BeforeAndAfterEach with SSLTest {

  implicit val timer: Timer[IO] = new TestTimer
  val requesterDN = DistinguishedName("CN=svc_heim_test1,OU=users,OU=Heimdali,DC=phdata,DC=io")
  val commonDescription = "Created by Archway integration tests."
  val workspaceName = "arch_workspace_integration_test"

  implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  override def beforeEach(): Unit = new Context {
    services.use { case (provisioning, workspaceService, _) =>
      for {
        maybeWorkspace <- workspaceService.findByName(workspaceName).value
        _ <- maybeWorkspace match {
          case Some(value) => for {
            _ <- logger.info("Deprovisioning workspace before test: " + value.id.get).pure[IO]
            fiber <- provisioning.attemptDeprovision(value)
            deprovisioned <- fiber.join
          } yield logger.info(s"Deprovisioning result " + deprovisioned).pure[IO]
          case None => logger.info(s"Deprovisioning is not needed").pure[IO]
        }
      } yield ()
    }
  }

  it should "Provision a simple workspace" in new Context {
    val templateRequest = TemplateRequest(
      workspaceName,
      "Archway simple workspace test",
      commonDescription,
      Compliance.empty,
      requesterDN
    )

    val result = provisionAndDestroyWorkspace(templateRequest, TemplateNames.simple)

    assert(
      workspaceProvisioningSuccess(result.unsafeRunSync())
    )
  }

  it should "Provision a user workspace" in new Context {
    val templateRequest = TemplateRequest(
      workspaceName,
      "Archway user workspace test",
      commonDescription,
      Compliance.empty,
      requesterDN
    )

    val result = provisionAndDestroyWorkspace(templateRequest, TemplateNames.user)

    assert(
      workspaceProvisioningSuccess(result.unsafeRunSync())
    )
  }

  it should "Provision a structured workspace" in new Context {
    val templateRequest = TemplateRequest(
      workspaceName,
      "Archway structured workspace test",
      commonDescription,
      Compliance.empty,
      requesterDN
    )

    val result = provisionAndDestroyWorkspace(templateRequest, TemplateNames.structured)

    assert(
      workspaceProvisioningSuccess(result.unsafeRunSync())
    )
  }

  private def workspaceProvisioningSuccess(provisionResult: NonEmptyList[Message])

    = {
    logger.info(s"Provisioning Test Result $provisionResult" )
    assert(provisionResult.length == 1)
    provisionResult.head match {
      case SimpleMessage(_, b) => b.contains("SUCCESS: ")
      case _ => throw new Exception("Provisioning failure")
    }
  }

  /**
    * Provision and destroy a workspace, returning the result from provisioning. A new test context is created for
    * each invocation of the function.
    *
    * @param templateRequest Template request object
    * @param templateName    "user", "simple", or "structured". Can be any template name found in 'templateRoot'
    * @return
    */
  private def provisionAndDestroyWorkspace(templateRequest: TemplateRequest,
                                           templateName: String)

    = new Context {
    val result = services.use { case (provisioning, workspaceService, templateService) =>
      for {
        maybeWorkspace <- workspaceService.findByName(workspaceName).value
        workspace <- maybeWorkspace match {
          case Some(existingWorkspace) => existingWorkspace.pure[IO]
          case None => for {
            workspaceRequest <- templateService.workspaceFor(templateRequest, templateName)
            createdWorkspace <- workspaceService.create(workspaceRequest)
          } yield createdWorkspace
        }
      provisioned <- for {
          // Get the workspace from the DB, the newly created workspace won't have all members filled
          // (like `HiveGrant.databaseName`, since it comes from the hive allocation)
          fetchedWorkspace <- workspaceService.findById(workspace.id.get).value.map(_.get)
          fiber <- provisioning.attemptProvision(fetchedWorkspace, 0)
          provisioned <- fiber.join
        } yield provisioned
      _ <- for {
            _ <- logger.info("Deprovisioning workspace: " + workspace.id.get).pure[IO]
            fiber <- provisioning.attemptDeprovision(workspace)
            deprovisioned <- fiber.join
        } yield logger.info(s"Deprovisioning result " + deprovisioned).pure[IO]
      } yield provisioned
    }
  }.result

  trait Context {
    implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

    implicit def timer = new TestTimer

    val provisionEC = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))
    val services = for {
      context <- AppContext.default[IO]()
      provisionService = new DefaultProvisioningService[IO](context, provisionEC)
      memberService = new MemberServiceImpl[IO](context)
      workspaceService = new WorkspaceServiceImpl[IO](provisionService, memberService, context)
      configService = new DBConfigService[IO](context)
      templateService = new JSONTemplateService[IO](context, configService)

      _ <- Resource.liftF(context.clusterCache.put(CacheEntry(0L, Seq.empty)))
    } yield (provisionService, workspaceService, templateService)
  }

}
