package com.heimdali.services

import cats.effect.IO
import org.apache.hadoop.security.UserGroupInformation
import org.scalatest.{FlatSpec, Matchers}

class UGILoginContextProviderSpec extends FlatSpec with Matchers {
  behavior of "UGILoginContextProvider"

  it should "log in as another user" in {

    val provider = new UGILoginContextProvider
    val result = provider.elevate[IO, String]("hdfs"){ () =>
      UserGroupInformation.getCurrentUser.getUserName
    }
    result.unsafeRunSync() shouldBe "hdfs"
  }
}
