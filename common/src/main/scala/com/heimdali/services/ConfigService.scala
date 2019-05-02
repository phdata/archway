package com.heimdali.services

trait ConfigService[F[_]] {

  def getTemplate(templateName: String): F[String]

}


