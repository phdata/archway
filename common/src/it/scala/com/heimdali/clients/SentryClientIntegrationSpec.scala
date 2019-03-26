package com.heimdali.clients

import cats.effect.IO
import com.heimdali.services.UGILoginContextProvider
import doobie._
import doobie.util.transactor.{Strategy, Transactor}
import org.apache.hadoop.security.UserGroupInformation
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class SentryClientIntegrationSpec extends FlatSpec with Matchers with HiveTest {

  behavior of "Sentry Client"

  ignore should "list roles" in {
    val client = new SentryClientImpl[IO](hiveTransactor, null, new UGILoginContextProvider())
    val result = client.roles.unsafeRunSync()
  }

}

trait HiveTest {

  val hiveTransactor: Transactor[IO] = {
    implicit val contextShift = IO.contextShift(ExecutionContext.global)
    System.setProperty("java.security.krb5.conf", getClass.getResource("/krb5.conf").getPath)
    UserGroupInformation.loginUserFromKeytab("benny@JOTUNN.IO", getClass.getResource("/heimdali.keytab").getPath)
    val initialHiveTransactor = Transactor.fromDriverManager[IO]("org.apache.hive.jdbc.HiveDriver", "jdbc:hive2://master1.jotunn.io:10000/default;principal=hive/_HOST@JOTUNN.IO", "", "")
    val strategy = Strategy.void.copy(always = FC.close)
    Transactor.strategy.set(initialHiveTransactor, strategy)
  }

}