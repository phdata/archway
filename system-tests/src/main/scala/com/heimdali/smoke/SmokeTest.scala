package com.heimdali.smoke

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.heimdali.smoke.ldap.LDAPSpec

object SmokeTest extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    (new LDAPSpec).execute()

    IO(println("Smoke testing")).as(ExitCode.Success)
  }
}