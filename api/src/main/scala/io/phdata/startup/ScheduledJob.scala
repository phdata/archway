/* Copyright 2018 phData Inc. */

package io.phdata.startup

trait ScheduledJob[F[_]] {

  def work: F[Unit]

}
