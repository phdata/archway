package com.heimdali.provisioning

import cats.data.NonEmptyList
import com.heimdali.models.{Application, LDAPRegistration}

trait ApplicationProvisioning {

  implicit val ApplicationProvisionable: Provisionable[Application] =
    Provisionable.deriveFromSteps { (application, _) =>
      NonEmptyList.of(
        TypeWith[Provisionable, LDAPRegistration](
          application.group
        ),
        TypeWith[Provisionable, ConsumerGroupGrant](
          ConsumerGroupGrant(application.id.get, application.consumerGroup, application.group.sentryRole)
        ),
      )
    }

}
