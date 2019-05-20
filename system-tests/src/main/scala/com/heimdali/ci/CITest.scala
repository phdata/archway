package com.heimdali.ci

import com.heimdali.ci.provisioning.ProvisioningSpec
import com.typesafe.scalalogging.LazyLogging

object CITest extends LazyLogging {

  def main(args: Array[String]): Unit = {
    (new ProvisioningSpec).execute()

    logger.info("CI testing")
  }
}
