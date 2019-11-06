package io.phdata.test.fixtures

import cats.effect._
import io.phdata.generators.{DefaultLDAPGroupGenerator, DefaultTopicGenerator}
import io.phdata.models.{DistinguishedName, KafkaTopic, TopicGrant}
import org.scalatest.{FlatSpec, Matchers}

class DefaultTopicGeneratorSpec extends FlatSpec with Matchers {

  behavior of "DefaultTopicGeneratorSpec"

  it should "generate a topic" in {
    implicit val timer: Timer[IO] = testTimer
    val configService = new TestConfigService()
    val ldapGroupGenerator = new DefaultLDAPGroupGenerator[IO](configService)
    val topicGenerator = new DefaultTopicGenerator[IO](appConfig, ldapGroupGenerator)
    val name = "sesame.topic_1"
    val groupName = "sesame_topic_1"

    val manager = ldapGroupGenerator.generate(groupName, DistinguishedName(s"cn=$groupName,${appConfig.ldap.groupPath}"), s"role_${groupName}").unsafeRunSync()
    val readonly = ldapGroupGenerator.generate(s"${groupName}_ro", DistinguishedName(s"cn=${groupName}_ro,${appConfig.ldap.groupPath}"), s"role_${groupName}_ro").unsafeRunSync()
    val expected = KafkaTopic(name, 1, 1, TopicGrant(name, manager, "read,describe"), TopicGrant(name, readonly, "read"))

    val actual = topicGenerator.topicFor("Topic #1", 1, 1, savedWorkspaceRequest).unsafeRunSync()

    actual shouldBe expected
  }

}
