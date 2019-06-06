package com.heimdali

package object provisioning
  extends LDAPRegistrationProvisioning
    with HiveProvisioning
    with ApplicationProvisioning
    with KafkaProvisioning
    with YarnProvisioning
    with WorkspaceRequestProvisioning {

  case class WorkspaceContext[F[_]](workspaceId: Long, context: AppContext[F])

}
