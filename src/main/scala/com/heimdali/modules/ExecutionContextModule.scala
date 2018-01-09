package com.heimdali.modules

import scala.concurrent.ExecutionContext

trait ExecutionContextModule {

  implicit val executionContext: ExecutionContext = ExecutionContext.global

}
