package io.phdata.provisioning

import cats.data.NonEmptyList
import io.phdata.models.Yarn

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
