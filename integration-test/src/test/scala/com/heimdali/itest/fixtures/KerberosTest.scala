package com.heimdali.itest.fixtures

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.security.UserGroupInformation

trait KerberosTest extends LazyLogging {
  val configuration = new Configuration()
  configuration.addResource(new File("./hive-conf/core-site.xml").toURI.toURL)
  configuration.addResource(new File("./hive-conf/hdfs-site.xml").toURI.toURL)
  configuration.addResource(new File("./hive-conf/hive-site.xml").toURI.toURL)
  configuration.addResource(new File("./sentry-conf/sentry-site.xml").toURI.toURL)

  System.setProperty("java.security.krb5.conf", new File(systemTestConfig.krb5FilePath).getPath)
  logger.info(s"kerberos test using principal: ${itestConfig.rest.principal}")
  logger.info(s"kerbusing keytab: ${itestConfig.rest.keytab}")

  UserGroupInformation.setConfiguration(configuration)

  UserGroupInformation.loginUserFromKeytab(itestConfig.rest.principal, new File(itestConfig.rest.keytab).getPath)
}
