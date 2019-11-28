package io.phdata.services

import java.util.UUID
import java.util.concurrent.Executors

import cats.effect.{ContextShift, IO}
import doobie.implicits._
import io.phdata.AppContext
import io.phdata.clients.KafkaClientImpl
import io.phdata.generators.{DefaultLDAPGroupGenerator, JsonTopicGenerator}
import io.phdata.itest.fixtures.{KerberosTest, SSLTest, _}
import io.phdata.models.{DistinguishedName, TopicRequest}
import io.phdata.provisioning.DefaultProvisioningService
import io.phdata.test.fixtures.{TestConfigService, TestTimer}
import kafka.admin.AdminUtils
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class KafkaServiceIntegrationSpec extends FlatSpec with Matchers with KerberosTest with LDAPTest {

  val userDN = DistinguishedName(s"CN=${systemTestConfig.existingUser},${itestConfig.ldap.userPath.get}")
  val workspaceId = 130L
  val topicRequest = TopicRequest(systemTestConfig.existingUser, s"it_kafka_topic_${UUID.randomUUID().toString.take(8)}", 1, 1)

  val client = new KafkaClientImpl[IO](itestConfig) {}

  behavior of "Kafka service"

  it should "create and provision Kafka topic" in new Context {
    val kafkaTopic = resources.use{ case(kafkaService, ctx, topicGenerator) =>
      for {
        _ <- kafkaService.create(userDN, workspaceId, topicRequest)

        workspace <- ctx.workspaceRequestRepository.find(workspaceId).value.transact(ctx.transactor)
        kafkaTopic <-
          topicGenerator.topicFor(topicRequest.name, topicRequest.partitions, topicRequest.replicationFactor, workspace.get)
      } yield kafkaTopic
    }.unsafeRunSync()

    AdminUtils.topicExists(client.zkUtils, kafkaTopic.name) shouldBe true
    validateLdapRegistrationProvisioning(kafkaTopic.managingRole.ldapRegistration)
    validateLdapRegistrationProvisioning(kafkaTopic.readonlyRole.ldapRegistration)
  }


  trait Context {
    implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    implicit def timer = new TestTimer
    val provisionEC = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(5))

    val resources = for {
      ctx <- AppContext.default[IO]()

      provisioningService =  new DefaultProvisioningService(ctx, provisionEC)
      ldapGroupGenerator = new DefaultLDAPGroupGenerator[IO](new TestConfigService)
      topicGenerator = new JsonTopicGenerator[IO](ctx.appConfig, ldapGroupGenerator)
    } yield (new KafkaServiceImpl[IO](ctx, provisioningService, topicGenerator), ctx, topicGenerator)
  }

}
