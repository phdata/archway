package io.phdata.generators

import cats.effect.{Clock, IO, Timer}
import io.phdata.models._
import io.phdata.test.fixtures.{AppContextProvider, TestConfigService, appConfig, savedWorkspaceRequest, testTimer}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class JsonTopicGeneratorSpec extends FlatSpec with Matchers with MockFactory with AppContextProvider {

  behavior of "JSON Topic Generator"

  it should "generate a valid json" in {
    implicit val timer: Timer[IO] = testTimer
    implicit val clock: Clock[IO] = timer.clock
    val context = genMockContext()
    val configService = new TestConfigService()
    val ldapGroupGenerator = new DefaultLDAPGroupGenerator[IO](configService)
    val generator = new JsonTopicGenerator[IO](appConfig, ldapGroupGenerator)

    val workspaceSystemName = TemplateRequest.generateName(savedWorkspaceRequest.name)
    val topicSystemName = TemplateRequest.generateName("Topic #1")
    val registeredName = s"$workspaceSystemName.$topicSystemName"
    val managerName = s"${workspaceSystemName}_$topicSystemName"
    val readonlyName = s"${workspaceSystemName}_${topicSystemName}_ro"

    val managerDistinguishedName = DistinguishedName(s"cn=$managerName,${context.appConfig.ldap.groupPath}")
    val readOnlyDistinguishedName = DistinguishedName(s"cn=$readonlyName,${context.appConfig.ldap.groupPath}")

    val expected = KafkaTopic(
      s"$workspaceSystemName.$topicSystemName", 1, 2,
      TopicGrant(registeredName,
        LDAPRegistration(managerDistinguishedName, managerName, s"role_$managerName",
          attributes = List(
            "dn" -> managerDistinguishedName.value,
            "objectClass" -> "group",
            "objectClass" -> "top",
            "sAMAccountName" -> managerName,
            "cn" -> managerName
          )
        ), "read,describe"),
      TopicGrant(registeredName,
        LDAPRegistration(readOnlyDistinguishedName, readonlyName, s"role_$readonlyName",
          attributes = List(
            "dn" -> readOnlyDistinguishedName.value,
            "objectClass" -> "group",
            "objectClass" -> "top",
            "sAMAccountName" -> readonlyName,
            "cn" -> readonlyName
          )
        ), "read")
    )

    val result = generator.topicFor("Topic #1", 1, 2, savedWorkspaceRequest).unsafeRunSync()
    println(result)

    assert(result.equals(expected))
  }

}
