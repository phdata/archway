package com.heimdali.services

import cats.effect.IO
import com.heimdali.test.fixtures._
import org.apache.hadoop.security.UserGroupInformation
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class UGILoginContextProviderSpec extends FlatSpec with Matchers with BeforeAndAfterAll {
  behavior of "UGILoginContextProvider"

  override def beforeAll(): Unit = {
    super.beforeAll()
    System.setProperty("java.security.krb5.conf", getClass.getResource("/krb5.conf").getPath)
  }

  it should "log in as another user" in {

    val provider = new UGILoginContextProvider(appConfig)
    val result = provider.elevate[IO, String]("hdfs"){ () =>
      UserGroupInformation.getCurrentUser.getUserName
    }
    result.unsafeRunSync() shouldBe "hdfs"
  }
}
