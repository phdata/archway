package com.heimdali.models

import java.time.Instant

import cats.Show
import cats.data.Kleisli
import cats.effect.Effect
import cats.implicits._
import com.heimdali.tasks.ProvisionTask._
import com.heimdali.tasks._
import io.circe._

case class LDAPRegistration(distinguishedName: String,
                            commonName: String,
                            sentryRole: String,
                            members: List[WorkspaceMember] = List.empty,
                            id: Option[Long] = None,
                            created: Option[Instant] = None)

object LDAPRegistration {

  implicit val show: Show[LDAPRegistration] =
    Show.show(l => s"creating AD/LDAP group ${l.commonName}")

  implicit def provisioner[F[_] : Effect]: ProvisionTask[F, LDAPRegistration] = ProvisionTask.instance { registration =>
//    val result = registration.members.map(m => AddMember(registration.distinguishedName, m.username).provision[F]).map(a => a)
    Kleisli
    for {
      group <- CreateLDAPGroup(registration.id.get, registration.commonName, registration.distinguishedName).provision[F]
      role <- CreateRole(registration.sentryRole).provision[F]
    } yield role |+| group
  }

  implicit val encoder: Encoder[LDAPRegistration] =
    Encoder.forProduct3("common_name", "distinguished_name", "sentry_role")(s => (s.commonName, s.distinguishedName, s.sentryRole))

  implicit final val decoder: Decoder[LDAPRegistration] =
    Decoder.forProduct3("common_name", "distinguished_name", "sentry_role")((cn: String, dn: String, role: String) => LDAPRegistration(dn, cn, role))

}