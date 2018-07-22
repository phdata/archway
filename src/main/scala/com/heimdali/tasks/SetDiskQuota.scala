package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import com.heimdali.models.AppContext
import doobie.implicits._

case class SetDiskQuota(workspaceId: Long, location: String, sizeInGB: Int)

object SetDiskQuota {

  implicit val show: Show[SetDiskQuota] =
    Show.show(s => s"""setting disk quota of ${s.sizeInGB}GB to "${s.location}""")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, SetDiskQuota] =
    ProvisionTask.instance[F, SetDiskQuota] { set =>
      Kleisli[F, AppContext[F], ProvisionResult] { config =>
        F.flatMap(F.attempt(config.hdfsClient.setQuota(set.location, set.sizeInGB))) {
          case Left(exception) => F.pure(Error(set, exception))
          case Right(_) =>
            F.map(config
              .databaseRepository
              .quotaSet(set.workspaceId)
              .transact(config.transactor)) { _ => Success(set) }
        }
      }
    }

}
