package com.heimdali.services

trait CacheService[F[_], A] {

  def initialize(work: F[A]) : F[Unit]

  def getOrRun(work: F[A]) : F[A]
}

