package io.phdata.provisioning

import cats.data.NonEmptyList
import io.phdata.models.{DistinguishedName, LDAPRegistration}

trait LDAPRegistrationProvisioning {

  implicit val LDAPRegistrationProvisionable: Provisionable[LDAPRegistration] =
    Provisionable.deriveFromSteps { (registration, _) =>
      NonEmptyList.of(
        TypeWith[Provisionable, ActiveDirectoryGroup](
          ActiveDirectoryGroup(
            registration.id.get,
            registration.commonName,
            registration.distinguishedName,
            registration.attributes
          )
        ),
        TypeWith[Provisionable, SentryRole](
          SentryRole(registration.id.get, registration.sentryRole)
        ),
        TypeWith[Provisionable, GroupGrant](
          GroupGrant(registration.id.get, registration.sentryRole, registration.commonName)
        )
      )
    }

}
