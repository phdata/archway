package com.heimdali.models

import scalikejdbc._

case class Compliance(id: Option[Long],
                      phiData: Boolean,
                      pciData: Boolean,
                      piiData: Boolean)

object Compliance extends SQLSyntaxSupport[Compliance] {
  def apply(g: ResultName[Compliance], rs: WrappedResultSet): Option[Compliance] =
    rs.longOpt(g.id).map { _ =>
      Compliance(
        rs.longOpt(g.id),
        rs.boolean(g.phiData),
        rs.boolean(g.pciData),
        rs.boolean(g.piiData)
      )
    }

}