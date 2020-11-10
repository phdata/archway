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
        )(ActiveDirectoryGroupProvisioning.provisionable, ActiveDirectoryGroupProvisioning.show),
        TypeWith[Provisionable, SecurityRole](
          SecurityRole(registration.id.get, registration.securityRole)
        )(RoleProvisioning.provisionable, RoleProvisioning.show),
        TypeWith[Provisionable, GroupGrant](
          GroupGrant(registration.id.get, registration.securityRole, registration.commonName)
        )(GroupGrantProvisioning.provisionable, GroupGrantProvisioning.show)
      )
    }

}
