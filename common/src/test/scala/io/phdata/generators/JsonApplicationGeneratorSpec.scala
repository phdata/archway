package io.phdata.generators

import cats.effect.{Clock, IO, Timer}
import io.phdata.models.{Application, DistinguishedName, LDAPRegistration, TemplateRequest}
import io.phdata.services.{ApplicationRequest, ConfigService}
import io.phdata.test.fixtures.{AppContextProvider, TestTimer, appConfig, _}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class JsonApplicationGeneratorSpec extends FlatSpec with Matchers with MockFactory with AppContextProvider {

  behavior of "JSON Application Generator"

  it should "generate a valid json" in {
    implicit val timer: Timer[IO] = new TestTimer
    implicit val clock: Clock[IO] = timer.clock

    val context = genMockContext()
    val configService: ConfigService[IO] = new TestConfigService()
    val ldapGroupGenerator = new DefaultLDAPGroupGenerator[IO](configService)

    val generator: ApplicationGenerator[IO] = new JsonApplicationGenerator[IO](context, ldapGroupGenerator)
    val applicationRequest = ApplicationRequest("Test application")

    val consumerGroup = s"${TemplateRequest.generateName(savedWorkspaceRequest.name)}_${applicationRequest.name}_cg"
    val distinguishedName = DistinguishedName(s"cn=$consumerGroup,${appConfig.ldap.groupPath}")
    val expected = Application(
      "Test application", consumerGroup,
      LDAPRegistration(distinguishedName, consumerGroup, s"role_$consumerGroup",
        attributes = List(
          "dn" -> distinguishedName.value,
          "objectClass" -> "group",
          "objectClass" -> "top",
          "sAMAccountName" -> consumerGroup,
          "cn" -> consumerGroup
        )
      )
    )

    val result: Application = generator.applicationFor(applicationRequest, savedWorkspaceRequest).unsafeRunSync()
    println(result)

    assert(result.equals(expected))
  }

}
