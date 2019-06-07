/* Copyright 2018 phData Inc. */

package com.heimdali.clients

import java.util.UUID

import cats.effect.{ContextShift, IO}
import com.heimdali.services.UGILoginContextProvider
import com.heimdali.test.fixtures._
import doobie._
import doobie.implicits._
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class HiveClientImplIntegrationSpec extends FlatSpec with Matchers with HiveTest with BeforeAndAfterAll {

  override implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val FOO_DB_NAME = s"zz_heimdali_hive_client_integration_${UUID.randomUUID().toString.take(8)}"

  val hiveClient = new HiveClientImpl[IO](new UGILoginContextProvider(appConfig), hiveTransactor)

  override def beforeAll(): Unit = {
    super.beforeAll()
    cleanup()
  }

  private def cleanup() = {
    runUpdate(Fragment.const(s"drop database if exists $FOO_DB_NAME"))
  }

  it should "Create a database" in {
    hiveClient.createDatabase(FOO_DB_NAME, "/tmp", "a comment", Map("pii_data" -> "true", "pci_data" -> "false"))
      .unsafeRunSync()

    assert(hiveClient.showDatabases().unsafeRunSync().contains(FOO_DB_NAME))
  }

  it should "Delete a database" in {
    hiveClient.createDatabase(FOO_DB_NAME, "/tmp", "a comment", Map("pii_data" -> "true", "pci_data" -> "false"))
      .unsafeRunSync()

    assert(hiveClient.showDatabases().unsafeRunSync().contains(FOO_DB_NAME))

    hiveClient.dropDatabase(FOO_DB_NAME).unsafeRunSync()
    assert(!hiveClient.showDatabases().unsafeRunSync().contains(FOO_DB_NAME))
  }

  private def runUpdate(f: Fragment): Int =
    f.update.run.transact(hiveTransactor).unsafeRunSync()

  override def afterAll(): Unit = {
    super.beforeAll()
    cleanup()
  }

}
