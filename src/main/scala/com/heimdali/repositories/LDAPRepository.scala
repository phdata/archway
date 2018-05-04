package com.heimdali.repositories

import com.heimdali.models.LDAPRegistration

import scala.concurrent.Future

trait LDAPRepository {

  def create(lDAPRegistration: LDAPRegistration): Future[LDAPRegistration]
}
