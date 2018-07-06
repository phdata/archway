package com.heimdali.models

import java.time.Instant

import io.circe._

case class LDAPRegistration(distinguishedName: String,
                            commonName: String,
                            sentryRole: String,
                            id: Option[Long] = None,
                            created: Option[Instant] = None) {

  import com.heimdali.tasks._

  val tasks: List[ProvisionTask] = List(
    CreateLDAPGroup(distinguishedName),
    CreateRole(sentryRole),
  )

}

object LDAPRegistration {

  implicit val encoder: Encoder[LDAPRegistration] =
    Encoder.forProduct3("common_name", "distinguished_name", "sentry_role")(s => (s.commonName, s.distinguishedName, s.sentryRole))

  implicit final val decoder: Decoder[LDAPRegistration] =
    Decoder.forProduct3("common_name", "distinguished_name", "sentry_role")((cn: String, dn: String, role: String) => LDAPRegistration(dn, cn, role))

}