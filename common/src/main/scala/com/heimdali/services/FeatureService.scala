package com.heimdali.services

import com.typesafe.scalalogging.LazyLogging

trait FeatureService[F[_]] {

  def isEnabled(feature: String): Boolean

  def all(): List[String]
}

class FeatureServiceImpl[F[_]](featureFlags: String) extends FeatureService[F] with LazyLogging {

  private val flagsList = featureFlags.split(",").toList

  override def isEnabled(feature: String): Boolean = {
    logger.info(s"Enabled feature flags: $featureFlags")
    logger.debug(s"Checking is $feature is enabled")
    flagsList.exists(flag => flag.trim.toLowerCase == feature.trim.toLowerCase)
  }

  override def all(): List[String] = flagsList
}
