package com.heimdali.models

import cats.effect.Effect
import cats.implicits._
import com.heimdali.tasks.ProvisionTask._
import com.heimdali.tasks.{CreateKafkaTopic, ProvisionTask}

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

}