package com.heimdali.services

import cats.effect.IO
import com.heimdali.itest.fixtures.KerberosTest
import com.heimdali.test.fixtures._
import org.apache.hadoop.security.UserGroupInformation
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class UGILoginContextProviderIntegrationSpec extends FlatSpec with Matchers with BeforeAndAfterAll with KerberosTest {
  behavior of "UGILoginContextProvider"

  it should "log in as another user" in {

    val provider = new UGILoginContextProvider(appConfig)
    val result = provider.elevate[IO, String]("hdfs"){ () =>
      UserGroupInformation.getCurrentUser.getUserName
    }
    result.unsafeRunSync() shouldBe "hdfs"
  }
}
