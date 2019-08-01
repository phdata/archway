package io.phdata.services

import io.phdata.models.Application

trait ApplicationService[F[_]] {

  def create(username: String, workspaceId: Long, applicationRrequest: ApplicationRequest): F[Application]

}
