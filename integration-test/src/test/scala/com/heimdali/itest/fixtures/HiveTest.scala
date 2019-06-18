/* Copyright 2018 phData Inc. */

package com.heimdali.itest.fixtures

import cats.effect.{ContextShift, IO}
import doobie.util.transactor.Transactor

trait HiveTest extends KerberosTest {

  implicit def contextShift: ContextShift[IO]

  val hiveTransactor: Transactor[IO] = itestConfig.db.hive.hiveTx

}