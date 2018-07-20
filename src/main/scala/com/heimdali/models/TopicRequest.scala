package com.heimdali.models

import cats.effect.Effect
import cats.implicits._
import com.heimdali.tasks.ProvisionTask._
import com.heimdali.tasks.{CreateKafkaTopic, ProvisionTask}
import io.circe._
import io.circe.syntax._


case class TopicRequest(name: String,
                        partitions: Int,
                        replicationFactor: Int)

object TopicRequest {

  implicit val encoder: Encoder[TopicRequest] =
    Encoder.instance { t =>
      Json.obj(
        "name" -> t.name.asJson,
        "partitions" -> t.partitions.asJson,
        "replication_factor" -> t.replicationFactor.asJson
      )
    }

  implicit val decoder: Decoder[TopicRequest] =
    Decoder.instance { t =>
      for {
        name <- t.downField("name").as[String]
        partitions <- t.downField("partitions").as[Int]
        replicationFactor <- t.downField("replication_factor").as[Int]
      } yield TopicRequest(name, partitions, replicationFactor)
    }

}
