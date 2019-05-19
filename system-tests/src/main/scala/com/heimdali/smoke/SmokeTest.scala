package com.heimdali.smoke

import com.heimdali.config.AppConfig._
import com.heimdali.config._
import com.heimdali.smoke.ldap.LDAPImplSmokeTest
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

object SmokeTest extends LazyLogging {

  def main(args: Array[String]): Unit = {
    val configFile = new java.io.File(args.toList.head)
    val config = ConfigFactory.parseFile(configFile)
    val Right(appConfig) = io.circe.config.parser.decodePath[AppConfig](config,"heimdali")

    logger.info(s"Running smoke tests with params: ${args.map(_.toString)}")

    new LDAPImplSmokeTest(appConfig).execute()
  }
}

