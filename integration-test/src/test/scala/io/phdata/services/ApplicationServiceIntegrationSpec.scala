package io.phdata.services

import java.util.UUID
import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO}
import io.phdata.AppContext
import io.phdata.generators.{DefaultLDAPGroupGenerator, JsonApplicationGenerator}
import io.phdata.itest.fixtures.{KerberosTest, LDAPTest, _}
import io.phdata.models.DistinguishedName
import io.phdata.provisioning.DefaultProvisioningService
import io.phdata.test.fixtures.{TestConfigService, TestTimer}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class ApplicationServiceIntegrationSpec extends FlatSpec with Matchers with KerberosTest with LDAPTest {

  val userDN = DistinguishedName(s"CN=${systemTestConfig.existingUser},${itestConfig.ldap.userPath.get}")
  val workspaceId = 130L
  val applicationRequest = ApplicationRequest(s"it_app_service_${UUID.randomUUID().toString.take(8)}")

  behavior of "Application Service"

  it should "Create and provision new Application" in new Context{
    val application = resources.use{ applicationService =>
      applicationService.create(userDN, workspaceId, applicationRequest)
    }.unsafeRunSync()

    validateLdapRegistrationProvisioning(application.group)
  }

  trait Context {
    implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    implicit def timer = new TestTimer
    val provisionEC = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))

    val resources = for {
      ctx <- AppContext.default[IO]()

      provisioningService =  new DefaultProvisioningService(ctx, provisionEC)
      ldapGroupGenerator = new DefaultLDAPGroupGenerator[IO](new TestConfigService)
      applicationGenerator = new JsonApplicationGenerator[IO](ctx, ldapGroupGenerator)
    } yield new ApplicationServiceImpl[IO](ctx, provisioningService, applicationGenerator)
  }


}
