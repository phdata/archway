/* Copyright 2018 phData Inc. */

package com.heimdali.startup

trait ScheduledJob[F[_]] {

  def work: F[Unit]

}
