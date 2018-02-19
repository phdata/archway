package com.heimdali.modules

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.hadoop.conf.Configuration

trait ConfigurationModule {

  val configuration: Config = ConfigFactory.load()

  val hadoopConfiguration: Configuration = {
    val config = new Configuration()
    config.addResource("core-site.xml")
    config.addResource("hdfs-site.xml")
    config.addResource("hive-site.xml")
    config
  }

}
