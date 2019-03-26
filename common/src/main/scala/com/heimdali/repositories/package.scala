package com.heimdali

import java.time.Instant

import com.heimdali.models._
import doobie._

package object repositories {

  case class LDAPRecord(distinguishedName: String,
                        commonName: String,
                        sentryRole: String,
                        id: Option[Long],
                        groupCreated: Option[Instant],
                        roleCreated: Option[Instant],
                        roleAssociated: Option[Instant])

  case class LDAPAttribute(key: String, value: String)

  implicit val dbRoleGetter: Get[DatabaseRole] =
    Get[String].map(DatabaseRole.unapply(_).get)

  implicit val approverRoleGetter: Get[ApproverRole] =
    Get[String].tmap(ApproverRole.parseRole)

  implicit val workspaceReader: Read[WorkspaceRequest] =
    Read[(String, String, String, String, String, Instant, Boolean, Boolean, Boolean, Option[Long], Boolean, Option[Long])].map {
      case (name, summary, description, behavior, requestedBy, requestDate, phiData, pciData, piiData, complianceId, singleUser, id) =>
        WorkspaceRequest(name, summary, description, behavior, requestedBy, requestDate, Compliance(phiData, pciData, piiData, complianceId), singleUser, id)
    }

  implicit def fromRecord(ldap: LDAPRecord): LDAPRegistration =
    LDAPRegistration(
      ldap.distinguishedName,
      ldap.commonName,
      ldap.sentryRole,
      ldap.id,
      ldap.groupCreated,
      ldap.roleCreated,
      ldap.roleAssociated
    )

}
