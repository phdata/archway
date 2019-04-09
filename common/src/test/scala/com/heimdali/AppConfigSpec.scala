package com.heimdali

import com.heimdali.config.AppConfig
import org.scalatest.FlatSpec

class AppConfigSpec extends FlatSpec {
  it should "Decode a config in " in {
    val appConfig = io.circe.config.parser.decodePath[AppConfig]("heimdali")

    appConfig match {
      case Left(e)  => fail(e)
      case Right(v) => // println(v)
    }

  }
}
