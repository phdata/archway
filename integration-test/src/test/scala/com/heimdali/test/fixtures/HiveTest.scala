/* Copyright 2018 phData Inc. */

package com.heimdali.test.fixtures

import cats.effect.{ContextShift, IO}
import doobie.LogHandler
import doobie.util.transactor.Transactor
import org.apache.hadoop.security.UserGroupInformation

trait HiveTest {

  implicit val logHandler = LogHandler.jdkLogHandler

  implicit def contextShift: ContextShift[IO]

  val hiveTransactor: Transactor[IO] = appConfig.db.hive.hiveTx

  System.setProperty("java.security.krb5.conf", getClass.getResource(systemTestConfig.krb5FilePath).getPath)
  UserGroupInformation.loginUserFromKeytab(appConfig.rest.principal, getClass.getResource(appConfig.rest.keytab).getPath)

}