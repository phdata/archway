package com.heimdali.itest.fixtures

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.security.UserGroupInformation

trait KerberosTest extends LazyLogging {
  System.setProperty("java.security.krb5.conf", new File(systemTestConfig.krb5FilePath).getPath)
  logger.info(s"kerberos test using principal: ${itestConfig.rest.principal}")
  logger.info(s"kerbusing keytab: ${itestConfig.rest.keytab}")

  UserGroupInformation.loginUserFromKeytab(itestConfig.rest.principal, new File(itestConfig.rest.keytab).getPath)
}
