package com.heimdali.ci

import com.heimdali.ci.provisioning.ProvisioningSpec

object CITest {

  def main(args: Array[String]): Unit = {
    (new ProvisioningSpec).execute()

    println("CI testing")
  }
}
