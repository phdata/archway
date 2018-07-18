package com.heimdali.tasks

import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import doobie.implicits._
import com.heimdali.models.AppContext
import com.heimdali.repositories.DatabaseRole

case class GrantLocationAccess(id: Long, roleName: String, location: String)

object GrantLocationAccess {

  implicit val show: Show[GrantLocationAccess] =
    Show.show(g => s"granting role ${g.roleName} rights to location ${g.location}")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, GrantLocationAccess] =
    ProvisionTask.instance { grant =>
      Kleisli[F, AppContext[F], ProvisionResult] { config =>
        F.flatMap(F.attempt(config.hiveClient.enableAccessToLocation(grant.location, grant.roleName))) {
          case Left(exception) => F.pure(Error(exception))
          case Right(_) =>
            F.map(config
              .databaseRepository
              .locationGranted(grant.role, grant.id)
              .transact(config.transactor)) { _ => Success[GrantLocationAccess] }
        }
      }
    }

}
