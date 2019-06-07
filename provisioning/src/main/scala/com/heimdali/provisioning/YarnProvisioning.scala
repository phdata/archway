package com.heimdali.provisioning

import cats.data.NonEmptyList
import com.heimdali.models.Yarn

trait YarnProvisioning {

  implicit val YarnProvisionable: Provisionable[Yarn] =
    Provisionable.deriveFromSteps { (yarn, _) =>
      NonEmptyList.one(
        TypeWith[Provisionable, ResourcePoolRegistration](
          ResourcePoolRegistration(yarn.id.get, yarn.poolName, yarn.maxCores, yarn.maxMemoryInGB)
        )
      )
    }

}
