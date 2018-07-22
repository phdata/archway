package com.heimdali.modules

import com.heimdali.config.AppConfig
import com.typesafe.config.{Config, ConfigFactory}
import kafka.utils.ZkUtils
import kafka.utils.ZKStringSerializer$

import org.I0Itec.zkclient.{ZkClient, ZkConnection}
import org.apache.hadoop.conf.Configuration
import pureconfig.{CamelCase, ConfigFieldMapping, ProductHint}

trait ConfigurationModule {

  val configuration: Config = ConfigFactory.load()

  val hadoopConfiguration: Configuration = {
    val config = new Configuration()
    config.addResource("core-site.xml")
    config.addResource("hdfs-site.xml")
    config.addResource("hive-site.xml")
    config.addResource("sentry-site.xml")
    config
  }

  private implicit def hint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))
  val Right(appConfig) = pureconfig.loadConfig[AppConfig]

  val zkConnectString: String =
    "master1.valhalla.phdata.io:2181,master2.valhalla.phdata.io:2181,master3.valhalla.phdata.io:2181"

  val sessionTimeOutInMs = 15 * 1000; // 15 secs
  val connectionTimeOutInMs = 10 * 1000; // 10 secs
  val zkClient = new ZkClient(zkConnectString, sessionTimeOutInMs, connectionTimeOutInMs, ZKStringSerializer$.MODULE$ )

  val zkUtils = new ZkUtils(zkClient, new ZkConnection(zkConnectString), false)

}
