package com.heimdali.models

import cats.effect.Effect
import cats.implicits._
import com.heimdali.tasks.ProvisionTask._
import com.heimdali.tasks.{CreateKafkaTopic, ProvisionTask}
import io.circe._
import io.circe.syntax._

case class KafkaTopic(name: String,
                      partitions: Int,
                      replicationFactor: Int,
                      managingGroup: LDAPRegistration,
                      id: Option[Long] = None)

object KafkaTopic {

  implicit def provisioner[F[_] : Effect]: ProvisionTask[F, KafkaTopic] =
    ProvisionTask.instance(topic =>
      for {
        create <- CreateKafkaTopic(topic.name, topic.partitions, topic.replicationFactor).provision
        managingGroup <- topic.managingGroup.provision
        readonly <- topic.managingGroup.provision
      } yield create |+| managingGroup |+| readonly
    )

  implicit val encoder: Encoder[KafkaTopic] =
    Encoder.instance { t =>
      Json.obj(
        "id" -> t.id.asJson,
        "name" -> t.name.asJson,
        "partitions" -> t.partitions.asJson,
        "replication_factor" -> t.replicationFactor.asJson,
        "managing_group" -> t.managingGroup.asJson
      )
    }

  implicit val decoder: Decoder[KafkaTopic] =
    Decoder.forProduct5("id", "name", "partitions", "replication_factor", "managing_group")((id: Option[Long], name: String, partitions: Int, replicationFactor: Int, group: LDAPRegistration) => KafkaTopic(name, partitions, replicationFactor, group, id))


}
