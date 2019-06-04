/* Copyright 2018 phData Inc. */

package com.heimdali.startup

import cats.effect._
import cats.implicits._
import com.heimdali.AppContext
import com.heimdali.config.ProvisioningConfig
import com.heimdali.services.ProvisioningService
import com.typesafe.scalalogging.LazyLogging

class Provisioning[F[_] : Sync : Timer : ContextShift](context: AppContext[F],
                                                       provisioningService: ProvisioningService[F])
  extends ScheduledJob[F] with LazyLogging {

  val provisioningConfig: ProvisioningConfig = context.appConfig.provisioning

  override def work: F[Unit] =
    for {
      _ <- logger.info("rectifying unprovisioned workspaces").pure[F]
      _ <- provisioningService.provisionAll()
      _ <- logger.info("provisioning going to sleep for {}", provisioningConfig.provisionInterval).pure[F]
      _ <- Timer[F].sleep(provisioningConfig.provisionInterval)
      _ <- work
    } yield ()

}