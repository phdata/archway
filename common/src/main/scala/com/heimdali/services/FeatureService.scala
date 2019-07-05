package com.heimdali.services

import com.typesafe.scalalogging.LazyLogging

trait FeatureService[F[_]] {

  def isEnabled(feature: String): Boolean

  def runIfEnabled[T](feature: String, action: => T, alternative: => T): T

  def all(): List[String]
}

class FeatureServiceImpl[F[_]](featureFlags: String) extends FeatureService[F] with LazyLogging {

  private val flagsList = featureFlags.split(",").toList

  override def isEnabled(feature: String): Boolean = {
    logger.info(s"Enabled feature flags: $featureFlags")
    logger.debug(s"Checking is $feature is enabled")
    flagsList.exists(flag => flag.trim.toLowerCase == feature.trim.toLowerCase)
  }

  override def runIfEnabled[T](feature: String, action: => T, alternative: => T): T = {
    if (isEnabled(feature)) action else alternative
  }

  override def all(): List[String] = flagsList
}
