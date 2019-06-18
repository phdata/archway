package com.heimdali.itest.fixtures

import java.io.File

import org.apache.hadoop.security.UserGroupInformation

trait KerberosTest {
  System.setProperty("java.security.krb5.conf", new File(systemTestConfig.krb5FilePath).getPath)
  println(s"using principal: ${itestConfig.rest.principal}")
  println(s"using keytab: ${itestConfig.rest.keytab}")

  UserGroupInformation.loginUserFromKeytab(itestConfig.rest.principal, new File(itestConfig.rest.keytab).getPath)
}
