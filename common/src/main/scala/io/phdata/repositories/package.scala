package io.phdata

import java.time.Instant

import io.phdata.models._
import com.typesafe.scalalogging.Logger
import doobie._
import doobie.util.log.{ExecFailure, LogHandler, ProcessingFailure, Success}

package object repositories {

  case class LDAPRecord(
      distinguishedName: DistinguishedName,
      commonName: String,
      sentryRole: String,
      id: Option[Long],
      groupCreated: Option[Instant],
      roleCreated: Option[Instant],
      roleAssociated: Option[Instant]
  )

  case class LDAPAttribute(key: String, value: String)

  implicit val dbRoleGetter: Get[DatabaseRole] =
    Get[String].map(DatabaseRole.unapply(_).get)

  implicit val approverRoleGetter: Get[ApproverRole] =
    Get[String].tmap(ApproverRole.parseRole)

  implicit val mapTagsReader: Read[Map[String, String]] =
    Read[(String, String)].map {
      case (
          key,
          value
          ) =>
        Map(key -> value)
    }

  implicit val metadataReader: Read[Metadata] =
    Read[(String, String, Int, Map[String, String])].map {
      case (
          name,
          description,
          ordering,
          tags
          ) =>
        Metadata(name, description, ordering, tags)
    }

  implicit val workspaceReader: Read[WorkspaceRequest] =
    Read[
      (String, String, String, String, String, Instant, Boolean, Boolean, Boolean, Option[Long], Boolean, Option[Long])
    ].map {
      case (
          name,
          summary,
          description,
          behavior,
          requestedBy,
          requestDate,
          phiData,
          pciData,
          piiData,
          complianceId,
          singleUser,
          id
          ) =>
        WorkspaceRequest(
          name,
          summary,
          description,
          behavior,
          DistinguishedName(requestedBy),
          requestDate,
          Compliance(phiData, pciData, piiData, complianceId),
          singleUser,
          id,
          metadata = Metadata(name, description, 0, Map.empty)
        )
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

  object CustomLogHandler {

    def logHandler(clazz: Class[_]): LogHandler = {
      val logger = Logger(clazz)
      LogHandler {

        case Success(s, a, e1, e2) =>
          logger.debug(s"""Successful Statement Execution:
                          |
            |  ${s.lines.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                          |
            | arguments = [${a.mkString(", ")}]
                          |   qwerty = ${e1.toMillis} ms exec + ${e2.toMillis} ms processing (${(e1 + e2).toMillis} ms total)
          """.stripMargin)

        case ProcessingFailure(s, a, e1, e2, t) =>
          logger.error(s"""Failed Statement Processing:
                          |
            |  ${s.lines.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                          |
            | arguments = [${a.mkString(", ")}]
                          |   elapsed = ${e1.toMillis} ms exec + ${e2.toMillis} ms processing (failed) (${(e1 + e2).toMillis} ms total)
                          |   failure = ${t.getMessage}
          """.stripMargin)

        case ExecFailure(s, a, e1, t) =>
          logger.error(s"""Failed Statement Execution:
                          |
            |  ${s.lines.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                          |
            | arguments = [${a.mkString(", ")}]
                          |   elapsed = ${e1.toMillis} ms exec (failed)
                          |   failure = ${t.getMessage}
          """.stripMargin)

      }
    }
  }

}
