package com.heimdali.services

import java.io.File
import javax.inject.Inject

import scala.sys.process._
import scala.concurrent.{ExecutionContext, Future}

trait KeytabService {
  def generateKeytab(principal: String): Future[String]
}

class KeytabServiceImpl @Inject() (implicit executionContext: ExecutionContext) extends KeytabService {
  override def generateKeytab(principal: String): Future[String] = Future {
    val temp = File.createTempFile(principal, "keytab")
    s"/usr/bin/generate_keytab.sh $${LDAP_BIND_DN} $${LDAP_BIND_PASSWORD} $principal  ${temp.getAbsolutePath} && cat ${temp.getAbsolutePath}" !!
  }
}