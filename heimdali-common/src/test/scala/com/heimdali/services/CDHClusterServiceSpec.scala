package com.heimdali.services

import cats.data.OptionT
import cats.effect.IO
import com.heimdali.config.ServiceOverride
import com.heimdali.test.fixtures.{HttpTest, _}
import org.apache.hadoop.conf.Configuration
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class CDHClusterServiceSpec
  extends FlatSpec
    with Matchers
    with MockFactory
    with HttpTest {

  behavior of "CDH Cluster service"

  it should "use hue override" in {
    val configuration = new Configuration()
    val hueConfigurationReader = mock[HueConfigurationReader[IO]]

    val newConfig = clusterConfig.copy(hueOverride = ServiceOverride(Some("abc"), Some(123)))

    val service = new CDHClusterService(httpClient, newConfig, configuration, hueConfigurationReader)

    val ClusterApp(_, _, _, _, actual) = service.hueApp(null, null, null, null)
    actual.head._2.head shouldBe AppLocation("abc", 123)
  }

  it should "return a cluster" in {
    val url = ""
    val name = "cluster"
    val version = "5.15.0"

    val username = "admin"
    val password = "admin"

    val hueConfigurationReader = mock[HueConfigurationReader[IO]]
    hueConfigurationReader.getValue _ expects "desktop.http_port" returning OptionT.some("808")

    val configuration = new Configuration()
    configuration.set("hive.server2.thrift.port", "888")
    configuration.set("yarn.nodemanager.webapp.address", "0.0.0.0:9998")
    configuration.set("yarn.resourcemanager.webapp.address", "0.0.0.0:9999")

    val service = new CDHClusterService(httpClient, clusterConfig, configuration, hueConfigurationReader)
    val list = service.list.unsafeRunSync()
    list should have length 1

    val first = list.head

    val impala = first.services.find(_.name == "impala").get
    impala.capabilities("beeswax").head.port shouldBe 21000
    impala.capabilities("hiveServer2").head.port shouldBe 21050

    val hive = first.services.find(_.name == "hive").get
    hive.capabilities("thrift").head.port shouldBe 888

    val hue = first.services.find(_.name == "hue").get
    hue.capabilities("load_balancer").head.port shouldBe 808

    val yarn = first.services.find(_.name == "yarn").get
    yarn.capabilities("node_manager").head.port shouldBe 9998
    yarn.capabilities("resource_manager").head.port shouldBe 9999

    first.id should be(name)
    first.name should be("Odin")
    first.distribution should be(CDH(version))
    first.status should be("GOOD_HEALTH")
  }
}
