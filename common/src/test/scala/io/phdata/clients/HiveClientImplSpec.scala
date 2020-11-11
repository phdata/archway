/* Copyright 2018 phData Inc. */

package io.phdata.clients

import cats.effect.IO
import org.scalatest.FlatSpec

class HiveClientImplSpec extends FlatSpec {

  val FOO_DB_NAME = "a_database"

  val hiveClient = new HiveClientImpl[IO](null)

  it should "Generate valid create database DDL" in {
    assertResult(s"CREATE DATABASE $FOO_DB_NAME  COMMENT  ? LOCATION  ? with DBPROPERTIES( ? = ? , ? = ? ) ")
    {
      hiveClient.createDatabaseStatement(FOO_DB_NAME, "/tmp", "a comment", Map("pii_data" -> "true", "pci_data" -> "false"))
        .update.sql}
  }

  it should "Create a db properties fragment" in {
    assertResult("with DBPROPERTIES( ? = ? , ? = ? ) "){
      hiveClient.createDBPropertiesFragment(Map("pii_data" -> "true", "pci_data" -> "false")).update.sql
    }
  }
}
