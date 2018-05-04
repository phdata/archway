package com.heimdali.repositories
import com.heimdali.models.LDAPRegistration
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}
import scalikejdbc._

class LDAPRepositoryImpl(implicit executionContext: ExecutionContext)
  extends LDAPRepository with LazyLogging {

  override def create(ldapRegistration: LDAPRegistration): Future[LDAPRegistration] = Future {
    logger.info("creating ldap registration {}")
    NamedDB('default) localTx { implicit session =>
      val id = applyUpdateAndReturnGeneratedKey {
        val l = LDAPRegistration.column
        insert.into(LDAPRegistration)
            .namedValues(
              l.distinguishedName -> ldapRegistration.distinguishedName,
              l.commonName -> ldapRegistration.commonName
            )
      }
      ldapRegistration.copy(id = Some(id))
    }
  }

}
