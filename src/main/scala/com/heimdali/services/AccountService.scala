package com.heimdali.services

import javax.inject.Inject

import be.objectify.deadbolt.scala.models.{Permission, Role, Subject}
import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.api.{Application, Configuration}
import play.api.libs.json.{Format, Json}

import scala.concurrent.{ExecutionContext, Future}

case class HeimdaliRole(name: String) extends Role

object HeimdaliRole {
  val BasicUser = HeimdaliRole("user")
}

case class User(name: String, username: String, role: HeimdaliRole) extends Subject {
  override val identifier: String = username
  override val permissions: List[Permission] = List.empty
  override val roles: List[Role] = List(role)
}

case class Token(accessToken: String, refreshToken: String)

object Token {
  implicit val tokenFormat: Format[Token] = Json.format[Token]
}

trait AccountService {
  def validate(token: String): Future[Option[User]]

  def login(username: String, password: String): Future[Option[User]]

  def refresh(user: User): Future[Token]
}

class LDAPAccountService @Inject()(ldapClient: LDAPClient,
                                   configuration: Configuration)
                                  (implicit val executionContext: ExecutionContext)
  extends AccountService {

  override def login(username: String, password: String): Future[Option[User]] =
    ldapClient.findUser(username, password) map {
      case Some(user) => Some(User(user.name, user.username, HeimdaliRole.BasicUser))
      case _ => None
    }

  override def refresh(user: User): Future[Token] = {
    val secret = configuration.get[String]("play.crypto.secret")

    val accessJson = Json.obj(
      "name" -> user.name,
      "username" -> user.username,
      "role" -> user.role.name
    )

    val refreshJson = Json.obj(
      "username" -> user.username
    )

    val accessToken = JwtJson.encode(accessJson, secret, JwtAlgorithm.HS256)
    val refreshToken = JwtJson.encode(refreshJson, secret, JwtAlgorithm.HS256)

    Future {
      Token(accessToken, refreshToken)
    }
  }

  override def validate(token: String): Future[Option[User]] = ???
}
