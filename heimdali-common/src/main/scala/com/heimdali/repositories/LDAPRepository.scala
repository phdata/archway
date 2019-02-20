package com.heimdali.repositories

import cats.implicits._
import cats.data.OptionT
import com.heimdali.models.LDAPRegistration
import doobie.free.connection.ConnectionIO

trait LDAPRepository {

  def create(lDAPRegistration: LDAPRegistration): ConnectionIO[LDAPRegistration]

  def complete(id: Long): ConnectionIO[LDAPRegistration]

  def findAll(resource: String, resourceId: Long): ConnectionIO[List[LDAPRegistration]]

  def find(resource: String, resourceId: Long, role: String): OptionT[ConnectionIO, LDAPRegistration]

  def groupCreated(id: Long): ConnectionIO[Int]

  def roleCreated(id: Long): ConnectionIO[Int]

  def groupAssociated(id: Long): ConnectionIO[Int]

}