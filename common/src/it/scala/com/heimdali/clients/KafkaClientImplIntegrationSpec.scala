package com.heimdali.clients

import cats.effect.IO
import kafka.admin.AdminUtils
import org.scalatest.{FlatSpec, Matchers}
import com.heimdali.test.fixtures.appConfig

class KafkaClientImplIntegrationSpec extends FlatSpec with Matchers {

  behavior of "KafkaImplCiTest"

  it should "create Kafka topic" in {
    val client = new KafkaClientImpl[IO](appConfig) {}
    val KAFKA_TEST_TOPIC = "heimdali_test_kafka_topic"

    client.createTopic(KAFKA_TEST_TOPIC, 10, 1).unsafeRunSync()

    AdminUtils.topicExists(client.zkUtils, KAFKA_TEST_TOPIC) shouldBe true
    AdminUtils.deleteTopic(client.zkUtils, KAFKA_TEST_TOPIC)
    AdminUtils.topicExists(client.zkUtils, KAFKA_TEST_TOPIC) shouldBe false
  }
}
