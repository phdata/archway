package com.heimdali.ci

import com.typesafe.scalalogging.LazyLogging

object CITest extends LazyLogging {

  def main(args: Array[String]): Unit = {
    logger.info("CI testing")
  }
}
