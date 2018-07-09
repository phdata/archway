package com.heimdali.models

import java.time.Instant

import cats.data.{Kleisli, NonEmptyList}
import cats.effect.IO
import com.heimdali.tasks._
import io.circe._
import cats.implicits._
import ProvisionTask._
import cats.Show

case class LDAPRegistration(distinguishedName: String,
                            commonName: String,
                            sentryRole: String,
                            id: Option[Long] = None,
                            created: Option[Instant] = None)

object LDAPRegistration {

  implicit val show: Show[LDAPRegistration] = ???

  implicit val provisioner: ProvisionTask[LDAPRegistration] =
    registration => for {
      res <- CreateLDAPGroup(registration.id.get, registration.commonName, registration.distinguishedName).provision
      _ <- CreateRole(registration.sentryRole).provision
    } yield Success[LDAPRegistration]("")

  implicit val encoder: Encoder[LDAPRegistration] =
    Encoder.forProduct3("common_name", "distinguished_name", "sentry_role")(s => (s.commonName, s.distinguishedName, s.sentryRole))

  implicit final val decoder: Decoder[LDAPRegistration] =
    Decoder.forProduct3("common_name", "distinguished_name", "sentry_role")((cn: String, dn: String, role: String) => LDAPRegistration(dn, cn, role))

}