/* Copyright 2018 phData Inc. */

package io.phdata.startup

import cats.effect._
import cats.implicits._
import io.phdata.config.ProvisioningConfig
import com.typesafe.scalalogging.LazyLogging
import io.phdata.AppContext
import io.phdata.services.ProvisioningService

class Provisioning[F[_]: Sync: Timer: ContextShift](context: AppContext[F], provisioningService: ProvisioningService[F])
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
