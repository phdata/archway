package com.heimdali.services

import com.typesafe.config.Config
import io.circe.Json
import pdi.jwt.{JwtAlgorithm, JwtCirce}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class User(name: String, username: String)

case class Token(accessToken: String, refreshToken: String)

trait AccountService {
  def validate(token: String): Future[Option[User]]

  def login(username: String, password: String): Future[Option[Token]]

  def refresh(user: User): Future[Token]
}

class LDAPAccountService(ldapClient: LDAPClient,
                         configuration: Config)
                        (implicit val executionContext: ExecutionContext)
  extends AccountService {

  val algo: JwtAlgorithm.HS256.type = JwtAlgorithm.HS256
  lazy val secret: String = configuration.getString("play.crypto.secret")

  override def login(username: String, password: String): Future[Option[Token]] =
    ldapClient.findUser(username, password) flatMap {
      case Some(user) =>
        refresh(User(user.name, user.username)).map(Some(_))
      case _ => Future(None)
    }

  override def refresh(user: User): Future[Token] = Future {
    val accessJson = Json.obj(
      "name" -> Json.fromString(user.name),
      "username" -> Json.fromString(user.username)
    )

    val refreshJson = Json.obj(
      "username" -> Json.fromString(user.username)
    )

    val accessToken = JwtCirce.encode(accessJson, secret, algo)
    val refreshToken = JwtCirce.encode(refreshJson, secret, algo)

    Token(accessToken, refreshToken)
  }

  override def validate(token: String): Future[Option[User]] = Future {
    import io.circe.generic.auto._
    JwtCirce.decodeJson(token, secret, Seq(algo)) match {
      case Success(json) => json.as[User].toOption
      case Failure(_) => None
    }
  }

}