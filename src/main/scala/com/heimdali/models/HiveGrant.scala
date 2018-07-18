package com.heimdali.models

import java.time.Instant

import cats._
import cats.effect._
import cats.implicits._
import com.heimdali.tasks.ProvisionTask._
import com.heimdali.tasks._
import io.circe._

case class HiveGrant(databaseName: String,
                     location: String,
                     ldapRegistration: LDAPRegistration,
                     id: Option[Long] = None,
                     locationAccess: Option[Instant] = None,
                     databaseAccess: Option[Instant] = None)

object HiveGrant {

  implicit val show: Show[HiveGrant] =
    Show.show(g => s"granting ${g.ldapRegistration.commonName} access to ${g.databaseName}")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, HiveGrant] =
    ProvisionTask.instance { grant =>
      for {
        db <- GrantDatabaseAccess(grant.id.get, grant.ldapRegistration.sentryRole, grant.databaseName).provision[F]
        location <- GrantLocationAccess(grant.id.get, grant.ldapRegistration.sentryRole, grant.location).provision[F]
      } yield db |+| location
    }

  implicit val encoder: Encoder[HiveGrant] = ???

  implicit val decoder: Decoder[HiveGrant] = ???

}