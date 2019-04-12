/* Copyright 2018 phData Inc. */

package com.heimdali.startup

import cats.effect.{Sync, Timer}
import com.heimdali.config.ProvisioningConfig
import com.heimdali.services.ProvisioningService

class Provisioning[F[_] : Sync](provisioningConfig: ProvisioningConfig,
                                provisioningService: ProvisioningService[F])
                               (implicit timer: Timer[F]) extends ScheduledJob[F] {
  override def start: F[Unit] =
    ScheduledJob.onInterval(provisioningService.provisionAll, provisioningConfig.provisionInterval)


}
