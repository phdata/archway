package com.heimdali.templates

import com.heimdali.models.Application

trait ApplicationGenerator[F[_]] {

  def applicationFor(name: String, workspaceSystemName: String): F[Application]

}
