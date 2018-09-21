package com.heimdali.clients

import cats.effect.IO
import doobie._
import doobie.util.transactor.{Strategy, Transactor}
import org.apache.hadoop.security.UserGroupInformation
import org.scalatest.{FlatSpec, Matchers}

class SentryClientSpec extends FlatSpec with Matchers {

  behavior of "Sentry Client"

  it should "list roles" in {
    System.setProperty("java.security.krb5.conf", getClass.getResource("/krb5.conf").getPath)
    UserGroupInformation.loginUserFromKeytab("benny@JOTUNN.IO", getClass.getResource("/heimdali.keytab").getPath)
    val initialHiveTransactor = Transactor.fromDriverManager[IO]("org.apache.hive.jdbc.HiveDriver", "jdbc:hive2://10.138.255.50:10000/default;principal=hive/_HOST@JOTUNN.IO", "", "")
    val strategy = Strategy.void.copy(always = FC.close)
    val hiveTransactor = Transactor.strategy.set(initialHiveTransactor, strategy)
    val client = new SentryClientImpl[IO](hiveTransactor, null)
    val result = client.roles.unsafeRunSync()
  }

}