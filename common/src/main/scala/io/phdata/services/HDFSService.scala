package io.phdata.services

import java.time.Instant

import cats.implicits._
import doobie.implicits._
import cats.effect.{Clock, Sync}
import doobie.util.transactor.Transactor
import io.phdata.clients.HDFSClient
import io.phdata.repositories.HiveAllocationRepository

trait HDFSService[F[_]] {

  def setQuota(path: String, sizeInGB: Int, resourceId: Long, instant: Instant): F[Unit]

  def removeQuota(path: String): F[Unit]
}

class HDFSServiceImpl[F[_]: Sync: Clock](
    hdfsClient: HDFSClient[F],
    hiveAllocationRepository: HiveAllocationRepository,
    transactor: Transactor[F]
) extends HDFSService[F] {

  override def setQuota(path: String, sizeInGB: Int, resourceId: Long, instant: Instant): F[Unit] = {
    hdfsClient.setQuota(path, sizeInGB).void *>
      hiveAllocationRepository.setQuota(resourceId, sizeInGB, instant).transact(transactor).void
  }

  override def removeQuota(path: String): F[Unit] = {
    hdfsClient.removeQuota(path).void
  }
}
