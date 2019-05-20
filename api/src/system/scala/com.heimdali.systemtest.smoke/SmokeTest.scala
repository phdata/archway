package com.heimdali.systemtest.smoke

import com.heimdali.systemtest.smoke.ldap.LDAPClientSmokeSpec


object SmokeTest {

  def main(args: Array[String]): Unit = {
    (new LDAPClientSmokeSpec).execute()
  }
}
