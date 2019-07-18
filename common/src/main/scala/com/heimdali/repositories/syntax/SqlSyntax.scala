package com.heimdali.repositories.syntax

import doobie.util.fragment.Fragment

trait SqlSyntax {

  val name: String

  val anonymousTable: Fragment
  
}

object SqlSyntax {

  def apply(driver: String): SqlSyntax = {
    if (driver.contains(ORACLE)) {
      new OracleSqlSyntax
    } else {
      new DefaultSqlSyntax
    }
  }

  def booleanToChar(v: Boolean): Char = v match {
    case true  => '1'
    case false => '0'
  }

  val ORACLE = "oracle"
  val DEFAULT = "default"

  val oracleSyntax = new OracleSqlSyntax
  val defaultSyntax = new DefaultSqlSyntax
}

class OracleSqlSyntax extends SqlSyntax {
  val name = SqlSyntax.ORACLE

  override val anonymousTable: Fragment = Fragment.const("")
}

class DefaultSqlSyntax extends SqlSyntax {
  val name = SqlSyntax.DEFAULT

  override val anonymousTable: Fragment = Fragment.const("as anon_table_sqlSyntax")
}
