package com.heimdali

import java.time.Instant

import com.heimdali.models.{ApproverRole, Compliance, DatabaseRole, WorkspaceRequest}
import doobie.util.{Get, Read}

package object repositories {

  implicit val dbRoleGetter: Get[DatabaseRole] =
    Get[String].map(DatabaseRole.unapply(_).get)

  implicit val approverRoleGetter: Get[ApproverRole] =
    Get[String].tmap(ApproverRole.parseRole)

  implicit val workspaceReader: Read[WorkspaceRequest] =
    Read[(String, String, String, String, String, Instant, Boolean, Boolean, Boolean, Option[Long], Boolean, Option[Long])].map {
      case (name, summary, description, behavior, requestedBy, requestDate, phiData, pciData, piiData, copmlianceId, singleUser, id) =>
        WorkspaceRequest(name, summary, description, behavior, requestedBy, requestDate, Compliance(phiData, pciData, piiData, copmlianceId), singleUser, id)
    }

}
