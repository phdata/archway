package com.heimdali.models

import scalikejdbc._

case class Yarn(id: Long,
                poolName: String,
                maxCores: Int,
                maxMemoryInGB: Int)

object Yarn extends SQLSyntaxSupport[Yarn] {

  def apply(g: ResultName[Yarn], rs: WrappedResultSet): Option[Yarn] =
    rs.longOpt(g.id).map { _ =>
      Yarn(
        rs.long(g.id),
        rs.string(g.poolName),
        rs.int(g.maxCores),
        rs.int(g.maxMemoryInGB)
      )
    }
}