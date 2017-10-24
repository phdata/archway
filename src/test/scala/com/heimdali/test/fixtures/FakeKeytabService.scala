package com.heimdali.test.fixtures

import javax.inject.Inject

import com.heimdali.services.KeytabService

import scala.concurrent.{ExecutionContext, Future}


class FakeKeytabService @Inject()(implicit executionContext: ExecutionContext) extends KeytabService {
  override def generateKeytab(principal: String): Future[String] = Future("/keytabs/keytab.keytab")
}
