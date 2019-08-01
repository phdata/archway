package io.phdata.services

import cats.effect.IO
import io.phdata.itest.fixtures.KerberosTest
import io.phdata.test.fixtures._
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
