package com.heimdali.models

import java.time.Instant

import cats._
import cats.effect._
import cats.implicits._
import com.heimdali.tasks.ProvisionTask._
import com.heimdali.tasks._
import io.circe._
import io.circe.java8.time._

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
        ldap <- grant.ldapRegistration.provision[F]
        db <- GrantDatabaseAccess(grant.id.get, grant.ldapRegistration.sentryRole, grant.databaseName).provision[F]
        location <- GrantLocationAccess(grant.id.get, grant.ldapRegistration.sentryRole, grant.location).provision[F]
      } yield ldap |+| db |+| location
    }

  implicit val encoder: Encoder[HiveGrant] =
    Encoder.forProduct3("location_access", "database_access", "group")(g => (g.locationAccess, g.databaseAccess, g.ldapRegistration))

  implicit val decoder: Decoder[HiveGrant] =
    Decoder.forProduct1("group")((g: LDAPRegistration) => HiveGrant("", "", g))

}
