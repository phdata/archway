package com.heimdali.services

import com.heimdali.models.Application

trait ApplicationService[F[_]] {

  def create(workspaceId: Long, applicationRrequest: ApplicationRequest): F[Application]

}

