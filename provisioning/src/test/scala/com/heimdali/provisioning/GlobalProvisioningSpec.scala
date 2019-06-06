package com.heimdali.provisioning

import cats.effect.{ContextShift, IO, Timer}
import cats.implicits._
import com.heimdali.clients.Kafka
import com.heimdali.provisioning.Provisionable.ops._
import com.heimdali.test.fixtures.{AppContextProvider, _}
import doobie._
import doobie.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class GlobalProvisioningSpec extends FlatSpec with MockFactory with AppContextProvider with Matchers {

  override implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val timer: Timer[IO] = testTimer

  it should "create and secure topics" in {
    val context = genMockContext(appConfig = appConfig.copy(kafka = appConfig.kafka.copy(secureTopics = true)))
    context.kafkaClient.createTopic _ expects("sesame.incoming", 1, 1) returning IO.unit
    context.kafkaRepository.topicCreated _ expects(id, testTimer.instant) returning 123.pure[ConnectionIO]
    context.provisioningLDAPClient.createGroup _ expects(savedLDAP.commonName, List.empty[(String, String)]) returning IO.unit twice()
    context.ldapRepository.groupCreated _ expects(id, testTimer.instant) returning 123.pure[ConnectionIO] twice()
    context.sentryClient.createRole _ expects savedLDAP.sentryRole returning IO.unit twice()
    context.ldapRepository.roleCreated _ expects(id, testTimer.instant) returning 123.pure[ConnectionIO] twice()
    context.sentryClient.grantGroup _ expects(savedLDAP.commonName, savedLDAP.sentryRole) returning IO.unit twice()
    context.ldapRepository.groupAssociated _ expects(id, testTimer.instant) returning 123.pure[ConnectionIO] twice()
    context.sentryClient.grantPrivilege _ expects(savedLDAP.sentryRole, Kafka, "Topic=sesame.incoming->action=all") returning IO.unit
    context.sentryClient.grantPrivilege _ expects(savedLDAP.sentryRole, Kafka, "Topic=sesame.incoming->action=read") returning IO.unit
    context.topicGrantRepository.topicAccess _ expects(id, testTimer.instant) returning 123.pure[ConnectionIO] twice()

    val (_, result) = savedTopic.provision[IO](WorkspaceContext(123L, context)).run.unsafeRunSync()

    result shouldBe Success
  }

  it should "create but skip securing topics" in {
    val context = genMockContext(appConfig = appConfig.copy(kafka = appConfig.kafka.copy(secureTopics = false)))
    context.kafkaClient.createTopic _ expects("sesame.incoming", 1, 1) returning IO.unit
    context.kafkaRepository.topicCreated _ expects(id, testTimer.instant) returning 123.pure[ConnectionIO]

    val (_, result) = savedTopic.provision[IO](WorkspaceContext(123L, context)).run.unsafeRunSync()

    result shouldBe Success
  }

}
