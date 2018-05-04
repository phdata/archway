package com.heimdali.models

import scalikejdbc._

case class HiveDatabase(id: Option[Long],
                        name: String,
                        role: String,
                        location: String,
                        sizeInGB: Int)

object HiveDatabase extends SQLSyntaxSupport[HiveDatabase] {
  def apply(g: ResultName[HiveDatabase], rs: WrappedResultSet): Option[HiveDatabase] =
    rs.longOpt(g.id).map { _ =>
      HiveDatabase(
        rs.longOpt(g.id),
        rs.string(g.name),
        rs.string(g.role),
        rs.string(g.location),
        rs.int(g.sizeInGB),
      )
    }
}