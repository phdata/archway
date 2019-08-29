package io.phdata.models

import cats.Show
import io.circe._
import io.circe.syntax._

case class KafkaTopic(
    name: String,
    partitions: Int,
    replicationFactor: Int,
    managingRole: TopicGrant,
    readonlyRole: TopicGrant,
    id: Option[Long] = None
)

object KafkaTopic {

  implicit val show: Show[KafkaTopic] =
    Show.show(k => s"${k.name} (p: ${k.partitions}, rf: ${k.replicationFactor}")

  implicit val encoder: Encoder[KafkaTopic] =
    Encoder.instance { t =>
      Json.obj(
        "id" -> t.id.asJson,
        "name" -> t.name.asJson,
        "partitions" -> t.partitions.asJson,
        "replication_factor" -> t.replicationFactor.asJson,
        "managing_role" -> t.managingRole.asJson,
        "readonly_role" -> t.readonlyRole.asJson
      )
    }

  implicit val decoder: Decoder[KafkaTopic] =
    Decoder.instance { t =>
      for {
        id <- t.downField("id").as[Option[Long]]
        name <- t.downField("name").as[String]
        partitions <- t.downField("partitions").as[Int]
        replicationFactor <- t.downField("replication_factor").as[Int]
        managingRole <- t.downField("managing_role").as[TopicGrant]
        readonlyRole <- t.downField("readonly_role").as[TopicGrant]
      } yield KafkaTopic(name, partitions, replicationFactor, managingRole, readonlyRole, id)
    }

}
