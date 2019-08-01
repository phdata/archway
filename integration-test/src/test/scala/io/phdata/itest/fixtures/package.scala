package io.phdata.itest

import java.io.File

import io.phdata.config.AppConfig
import com.typesafe.config.ConfigFactory
import io.circe.Decoder

package object fixtures {

  case class SystemTestConfig(existingUser: String, existingPassword: String, krb5FilePath: String)

  object SystemTestConfig {
    import io.circe.generic.semiauto._

    implicit val systemTestConf: Decoder[SystemTestConfig] = deriveDecoder
  }

  System.setProperty("HEIMDALI_API_SERVICE_PRINCIPAL", "heimdali_api/edge1.valhalla.phdata.io@PHDATA.IO")

  val config = ConfigFactory.parseFile(new File("itest-config/application.itest.conf"))
  val Right(itestConfig) = io.circe.config.parser.decodePath[AppConfig](config.resolve(), "heimdali")
  val sysConfig = ConfigFactory.parseFile(new File("itest-config/system-test.conf"))
  val Right(systemTestConfig) = io.circe.config.parser.decode[SystemTestConfig](sysConfig.resolve())
}
