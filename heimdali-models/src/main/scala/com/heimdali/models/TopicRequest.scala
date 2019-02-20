package com.heimdali.models

import cats.effect.Effect
import cats.implicits._
import io.circe._
import io.circe.syntax._

case class TopicRequest(username: String,
                        name: String,
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

  def decoder(username: String): Decoder[TopicRequest] =
    Decoder.instance { t =>
      for {
        name <- t.downField("name").as[String]
        partitions <- t.downField("partitions").as[Int]
        replicationFactor <- t.downField("replication_factor").as[Int]
      } yield TopicRequest(username, name, partitions, replicationFactor)
    }

}
