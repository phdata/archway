package com.heimdali.services

import javax.inject.Inject

import be.objectify.deadbolt.scala.models.{Permission, Role, Subject}

import scala.concurrent.{ExecutionContext, Future}

case class HeimdaliRole(name: String) extends Role

object HeimdaliRole {
  val ADMIN = "admin"
  val USER = "user"
  val GOVERNANCE = "governance"
}

case class User(name: String, username: String, role: HeimdaliRole) extends Subject {
  override val identifier: String = username
  override val permissions: List[Permission] = List.empty
  override val roles: List[Role] = List(role)
}

trait AccountService {
  def login(username: String, password: String): Future[Option[User]]
}

case class LDAPUser(name: String, username: String, password: String, memberships: Seq[String])

trait LDAPClient {
  def findUser(username: String): Future[Option[LDAPUser]]
}

class LDAPAccountService @Inject() (ldapClient: LDAPClient)(implicit val executionContext: ExecutionContext)
  extends AccountService {

  override def login(username: String, password: String): Future[Option[User]] =
    ldapClient.findUser(username) map {
      case Some(user) if user.password == password => Some(User(user.name, user.username, HeimdaliRole("user")))
      case _ => None
    }

}
