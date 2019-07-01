package com.heimdali.services

import java.io.File

import cats.Applicative

import scala.sys.process._

trait KeytabService[F[_]] {
  def generateKeytab(principal: String)(implicit evidence: Applicative[F]): F[String]
}

class KeytabServiceImpl[F[_]] extends KeytabService[F] {

  override def generateKeytab(principal: String)(implicit evidence: Applicative[F]): F[String] =
    evidence.pure {
      val temp = File.createTempFile(principal, "keytab")
      s"/usr/bin/generate_keytab.sh $${LDAP_BIND_DN} $${LDAP_BIND_PASSWORD} $principal  ${temp.getAbsolutePath} && cat ${temp.getAbsolutePath}" !!
    }

}
