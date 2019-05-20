package com.heimdali.smoke

import com.heimdali.smoke.ldap.LDAPSpec

object SmokeTest {

  def main(args: Array[String]): Unit = {
    (new LDAPSpec).execute()

    println("Smoke testing")
  }


}
