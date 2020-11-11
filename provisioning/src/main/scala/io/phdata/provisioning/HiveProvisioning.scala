package io.phdata.provisioning

import cats.data.NonEmptyList
import io.phdata.models.{HiveAllocation, HiveGrant, LDAPRegistration}

trait HiveProvisioning {

  val HiveGrantProvisionable: Provisionable[HiveGrant] =
    Provisionable.deriveFromSteps { (hiveGrant, _) =>
      NonEmptyList.of(
        TypeWith[Provisionable, LDAPRegistration](
          hiveGrant.ldapRegistration
        )(LDAPRegistrationProvisionable, LDAPRegistration.show),
        TypeWith[Provisionable, DatabaseGrant](
          DatabaseGrant(
            hiveGrant.id.get,
            hiveGrant.ldapRegistration.securityRole,
            hiveGrant.databaseName,
            hiveGrant.databaseRole
          )
        )(DatabaseGrantProvisioning.databaseGrantProvisionable, DatabaseGrantProvisioning.show),
        TypeWith[Provisionable, LocationGrant](
          LocationGrant(hiveGrant.id.get, hiveGrant.ldapRegistration.securityRole, hiveGrant.location)
        )(LocationGrantProvisioning.provisionable, LocationGrantProvisioning.show)
      )
    }

  val HiveAllocationProvisionable: Provisionable[HiveAllocation] =
    Provisionable.deriveFromSteps { (hiveAllocation, _) =>
      NonEmptyList.of(
        TypeWith[Provisionable, HiveDatabaseRegistration](
          HiveDatabaseRegistration(hiveAllocation.id.get, hiveAllocation.name, hiveAllocation.location)
        )(HiveDatabaseRegistrationProvisioning.provisionable, HiveDatabaseRegistrationProvisioning.viewer),
        TypeWith[Provisionable, HiveGrant](
          hiveAllocation.managingGroup
        )(HiveGrantProvisionable, HiveGrant.show)
      ) ++
        hiveAllocation.readWriteGroup.map { readwrite =>
          TypeWith[Provisionable, HiveGrant](
            readwrite
          )(HiveGrantProvisionable, HiveGrant.show)
        }.toList ++
        hiveAllocation.readonlyGroup.map { readonly =>
          TypeWith[Provisionable, HiveGrant](
            readonly
          )(HiveGrantProvisionable, HiveGrant.show)
        }.toList
    }
}
