package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.IO
import com.heimdali.models.AppConfig

case class SetDiskQuota(location: String, sizeInGB: Int)

object SetDiskQuota {

  implicit val show: Show[SetDiskQuota] =
    Show.show(s => s"""setting disk quota of ${s.sizeInGB}GB to "${s.location}""")

  implicit val provisioner: ProvisionTask[SetDiskQuota] =
    set => Kleisli[IO, AppConfig, ProvisionResult] { config =>
      config.hdfsClient.setQuota(set.location, set.sizeInGB).attempt.map {
        case Left(exception) => Error(exception)
        case Right(_) => Success[SetDiskQuota]
      }
    }

}
