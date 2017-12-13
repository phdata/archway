package com.heimdali

import akka.http.scaladsl.model.headers.{BasicHttpCredentials, HttpChallenge, HttpChallenges, HttpCredentials}
import akka.http.scaladsl.server.directives.{AuthenticationResult, Credentials, SecurityDirectives}
import com.heimdali.services.{AccountService, Token, User}

import scala.concurrent.{ExecutionContext, Future}

trait AuthService {
  def validateCredentials(creds: Option[HttpCredentials]): Future[Either[HttpChallenge, SecurityDirectives.AuthenticationResult[Token]]]

  def validateToken(creds: Credentials): Future[Option[User]]
}

class AuthServiceImpl(accountService: AccountService)
                     (implicit executionContext: ExecutionContext) extends AuthService {

  override def validateCredentials(creds: Option[HttpCredentials]): Future[Either[HttpChallenge, SecurityDirectives.AuthenticationResult[Token]]] =
    creds match {
      case Some(BasicHttpCredentials(username, password)) =>
        accountService.login(username, password).map {
          case Some(user) => Right(AuthenticationResult.success(user))
          case None => Left(HttpChallenges.basic("heimdali"))
        }
    }

  override def validateToken(creds: Credentials): Future[Option[User]] =
    creds match {
      case Credentials.Provided(token) =>
        accountService.validate(token)
      case _ =>
        Future(None)
    }

}