package com.heimdali.modules

import java.io.File
import java.time.Clock

import com.heimdali.config.AppConfig
import com.heimdali.services.{LoginContextProvider, UGILoginContextProvider}
import com.typesafe.config.{Config, ConfigFactory}
import kafka.utils.ZkUtils
import kafka.utils.ZKStringSerializer$
import org.I0Itec.zkclient.{ZkClient, ZkConnection}
import org.apache.hadoop.conf.Configuration
import pureconfig.{CamelCase, ConfigFieldMapping, ProductHint}

trait ConfigurationModule {

  implicit val clock: Clock = Clock.systemUTC()

  val hadoopConfiguration: Configuration = {
    val config = new Configuration()
    config.addResource(new File("./hive-conf/core-site.xml").toURI.toURL)
    config.addResource(new File("./hive-conf/hdfs-site.xml").toURI.toURL)
    config.addResource(new File("./hive-conf/hive-site.xml").toURI.toURL)
    config.addResource(new File("./sentry-conf/sentry-site.xml").toURI.toURL)
    config
  }

  private implicit def hint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))
  val Right(appConfig) = pureconfig.loadConfig[AppConfig]

  val sessionTimeOutInMs = 15 * 1000; // 15 secs
  val connectionTimeOutInMs = 10 * 1000; // 10 secs
  val zkClient = new ZkClient(appConfig.kafka.zookeeperConnect, sessionTimeOutInMs, connectionTimeOutInMs, ZKStringSerializer$.MODULE$ )

  val zkUtils = new ZkUtils(zkClient, new ZkConnection(appConfig.kafka.zookeeperConnect), false)


  val loginContextProvider: LoginContextProvider = new UGILoginContextProvider()

}
