package com.heimdali.test.fixtures

import org.scalatest.{BeforeAndAfterEach, Suite}
import scalikejdbc._

trait DBTest extends BeforeAndAfterEach { this: Suite =>
  def tables: Seq[SQLSyntaxSupport[_]]

  Class.forName("org.postgresql.Driver")
  ConnectionPool.add('default, "jdbc:postgresql://localhost/heimdali", "postgres", "postgres")

  override protected def beforeEach(): Unit = {
    NamedDB('default) localTx { implicit session =>
      tables.foreach(table =>
        applyUpdate {
          delete.from(table)
        }
      )
    }
  }
}
