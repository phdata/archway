/* Copyright 2018 phData Inc. */

package com.heimdali.test.fixtures

import cats.effect.{ContextShift, IO}
import doobie.util.transactor.{Strategy, Transactor}
import doobie.{FC, LogHandler}
import org.apache.hadoop.security.UserGroupInformation

trait HiveTest {

  implicit val logHandler = LogHandler.jdkLogHandler

  implicit def contextShift: ContextShift[IO]

  val hiveTransactor: Transactor[IO] = {
    System.setProperty("java.security.krb5.conf", getClass.getResource("/krb5.conf").getPath)
    UserGroupInformation.loginUserFromKeytab("benny@JOTUNN.IO", getClass.getResource("/heimdali.keytab").getPath)
    val initialHiveTransactor = Transactor.fromDriverManager[IO]("org.apache.hive.jdbc.HiveDriver", "jdbc:hive2://master1.jotunn.io:10000/default;principal=hive/_HOST@JOTUNN.IO", "", "")
    val strategy = Strategy.void.copy(always = FC.close)
    Transactor.strategy.set(initialHiveTransactor, strategy)
  }

}