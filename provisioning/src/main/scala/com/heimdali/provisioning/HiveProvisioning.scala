package com.heimdali.provisioning

import cats.data.NonEmptyList
import com.heimdali.models.{HiveAllocation, HiveGrant, LDAPRegistration}

trait HiveProvisioning {

  implicit val HiveGrantProvisionable: Provisionable[HiveGrant] =
    Provisionable.deriveFromSteps { (hiveGrant, _) =>
      NonEmptyList.of(
        TypeWith[Provisionable, LDAPRegistration](
          hiveGrant.ldapRegistration
        ),
        TypeWith[Provisionable, DatabaseGrant](
          DatabaseGrant(hiveGrant.id.get, hiveGrant.ldapRegistration.sentryRole, hiveGrant.databaseName, hiveGrant.databaseRole)
        ),
        TypeWith[Provisionable, LocationGrant](
          LocationGrant(hiveGrant.id.get, hiveGrant.ldapRegistration.sentryRole, hiveGrant.location)
        ),
      )
    }

  implicit val HiveAllocationProvisionable: Provisionable[HiveAllocation] =
    Provisionable.deriveFromSteps { (hiveAllocation, _) =>
      NonEmptyList.of(
        TypeWith[Provisionable, DatabaseDirectory](
          DatabaseDirectory(hiveAllocation.id.get, hiveAllocation.location, None)
        ),
        TypeWith[Provisionable, DiskQuota](
          DiskQuota(hiveAllocation.id.get, hiveAllocation.location, hiveAllocation.sizeInGB)
        ),
        TypeWith[Provisionable, HiveDatabaseRegistration](
          HiveDatabaseRegistration(hiveAllocation.id.get, hiveAllocation.name, hiveAllocation.location)
        ),
        TypeWith[Provisionable, HiveGrant](
          hiveAllocation.managingGroup
        ),
      ) ++
        hiveAllocation.readWriteGroup.map { readwrite =>
          TypeWith[Provisionable, HiveGrant](
            readwrite
          )
        }.toList ++
        hiveAllocation.readonlyGroup.map { readonly =>
          TypeWith[Provisionable, HiveGrant](
            readonly
          )
        }.toList
    }
}
