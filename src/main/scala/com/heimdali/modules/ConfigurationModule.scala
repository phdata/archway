package com.heimdali.modules

import com.heimdali.config.AppConfig
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.hadoop.conf.Configuration
import pureconfig.{CamelCase, ConfigFieldMapping, ProductHint}

trait ConfigurationModule {

  val configuration: Config = ConfigFactory.load()

  val hadoopConfiguration: Configuration = {
    val config = new Configuration()
    config.addResource("core-site.xml")
    config.addResource("hdfs-site.xml")
    config.addResource("hive-site.xml")
    config
  }

  private implicit def hint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))
  val Right(appConfig) = pureconfig.loadConfig[AppConfig]

}
