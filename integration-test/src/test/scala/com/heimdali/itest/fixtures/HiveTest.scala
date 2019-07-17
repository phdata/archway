/* Copyright 2018 phData Inc. */

package com.heimdali.itest.fixtures

import cats.effect.{ContextShift, IO}
import doobie.util.transactor.Transactor

import scala.concurrent.ExecutionContext

trait HiveTest extends KerberosTest {

  implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val hiveTransactor: Transactor[IO] = itestConfig.db.hive.hiveTx

}