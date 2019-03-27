package com.heimdali.provisioning

import java.time.Instant

import cats.Show
import cats.data.Kleisli
import cats.effect.{Effect, Timer}
import cats.implicits._
import com.heimdali.AppContext
import doobie.implicits._

case class SetDiskQuota(workspaceId: Long, location: String, sizeInGB: Int)

object SetDiskQuota {

  implicit val show: Show[SetDiskQuota] =
    Show.show(s => s"""setting disk quota of ${s.sizeInGB}GB to "${s.location}""")

  implicit def provisioner[F[_] : Effect : Timer]: ProvisionTask[F, SetDiskQuota] =
    ProvisionTask.instance[F, SetDiskQuota] { set =>
      Kleisli[F, WorkspaceContext[F], ProvisionResult] { case (id, context) =>
        context
          .hdfsClient
          .setQuota(set.location, set.sizeInGB)
          .attempt
          .flatMap {
            case Left(exception) => Effect[F].pure(Error(id, set, exception))
            case Right(_) =>
              for {
                time <- Timer[F].clock.realTime(scala.concurrent.duration.MILLISECONDS)
                _ <- context
                  .databaseRepository
                  .quotaSet(set.workspaceId, Instant.ofEpochMilli(time))
                  .transact(context.transactor)
              } yield Success(id, set)
          }
      }
    }

}
