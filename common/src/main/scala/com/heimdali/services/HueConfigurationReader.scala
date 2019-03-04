package com.heimdali.services

import cats.data.OptionT

trait HueConfigurationReader[F[_]] {

  def getValue(path: String): OptionT[F, String]

}
