package com.heimdali.models

import scalikejdbc._

case class LDAPRegistration(id: Option[Long],
                            distinguishedName: String,
                            commonName: String)

object LDAPRegistration extends SQLSyntaxSupport[LDAPRegistration] {
  def apply(g: ResultName[LDAPRegistration], rs: WrappedResultSet): Option[LDAPRegistration] =
    rs.longOpt(g.id).map { _ =>
      LDAPRegistration(
        rs.longOpt(g.id),
        rs.string(g.distinguishedName),
        rs.string(g.commonName)
      )
    }
}