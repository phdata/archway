package com.heimdali.services

import cats.data.OptionT
import cats.effect.Sync
import org.ini4j.Ini

import scala.io.Source

class IniHueConfigurationReader[F[_]](implicit F: Sync[F]) extends HueConfigurationReader[F] {

  val hueConfFilename = "hue-conf/hue.ini"

  override def getValue(path: String): OptionT[F, String] =
    OptionT(F.delay {
      val ini = new Ini(Source.fromResource(hueConfFilename).bufferedReader())
      path.split("\\.").toList match {
        case section :: key :: Nil =>
          Option(ini.get(section, key))
        case _ =>
          None
      }
    })

}
