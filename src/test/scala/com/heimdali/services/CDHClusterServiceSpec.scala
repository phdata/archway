package com.heimdali.services

import com.heimdali.clients.HttpTest
import com.heimdali.test.fixtures._
import org.apache.hadoop.conf.Configuration
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class CDHClusterServiceSpec
  extends FlatSpec
    with Matchers
    with MockFactory
    with HttpTest {

  behavior of "CDH Cluster service"

  it should "return a cluster" in {
    val url = ""
    val name = "cluster"
    val version = "5.15.0"

    val username = "admin"
    val password = "admin"

    val service = new CDHClusterService(httpClient, clusterConfig, new Configuration())
    val list = service.list.unsafeRunSync()
    list should have length 1
    list.head.id should be(name)
    list.head.name should be("Odin")
    list.head.distribution should be(CDH(version))
    list.head.status should be("GOOD_HEALTH")
  }
}
