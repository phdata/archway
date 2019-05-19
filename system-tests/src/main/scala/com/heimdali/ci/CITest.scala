package com.heimdali.ci

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.heimdali.ci.provisioning.ProvisioningSpec

object CITest extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    (new ProvisioningSpec).execute()

    IO(println("CI testing")).as(ExitCode.Success)
  }
}
