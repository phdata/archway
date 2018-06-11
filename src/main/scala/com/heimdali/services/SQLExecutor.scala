package com.heimdali.services

import scala.concurrent.Future

trait SQLExecutor {

  def execute(sql: String): Future[Boolean]

}
