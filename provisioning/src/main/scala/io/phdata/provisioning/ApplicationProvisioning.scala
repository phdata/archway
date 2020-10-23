package io.phdata.provisioning

import cats.data.NonEmptyList
import io.phdata.models.{Application, LDAPRegistration}

trait ApplicationProvisioning {

  implicit val ApplicationProvisionable: Provisionable[Application] =
    Provisionable.deriveFromSteps { (application, _) =>
      NonEmptyList.of(
        TypeWith[Provisionable, LDAPRegistration](
          application.group
        )(LDAPRegistrationProvisionable, LDAPRegistration.show)
      )
    }

}
