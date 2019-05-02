/* Copyright 2018 phData Inc. */

package com.heimdali.startup

import cats.effect.{ContextShift, Sync, Timer}
import com.heimdali.config.ProvisioningConfig
import com.heimdali.services.ProvisioningService

class Provisioning[F[_] : Sync : ContextShift : Timer](provisioningConfig: ProvisioningConfig,
                                                       provisioningService: ProvisioningService[F])
  extends ScheduledJob[F] {

  override def stream: fs2.Stream[F, Unit] =
    ScheduledJob.onInterval(provisioningService.provisionAll, provisioningConfig.provisionInterval)

}
