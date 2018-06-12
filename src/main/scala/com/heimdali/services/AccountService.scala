package com.heimdali.services

import cats.data.{EitherT, OptionT}
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.heimdali.clients.{LDAPClient, LDAPUser}
import com.heimdali.config.{ApprovalConfig, RestConfig}
import com.heimdali.models.{Token, User, UserPermissions}
import com.typesafe.scalalogging.LazyLogging
import io.circe.Json
import io.circe.syntax._
import pdi.jwt.algorithms.JwtHmacAlgorithm
import pdi.jwt.{JwtAlgorithm, JwtCirce}

trait AccountService[F[_]] {
  def validate(token: String): EitherT[F, Throwable, User]

  def login(username: String, password: String): OptionT[F, Token]

  def refresh(user: User): F[Token]
}

class AccountServiceImpl[F[_] : Sync](ldapClient: LDAPClient[F],
                                      restConfig: RestConfig,
                                      approvalConfig: ApprovalConfig)
  extends AccountService[F]
    with LazyLogging {

  private val algo: JwtAlgorithm.HS512.type = JwtAlgorithm.HS512

  implicit def convertUser(ldapUser: LDAPUser): User =
    User(ldapUser.name,
      ldapUser.username,
      UserPermissions(riskManagement = ldapUser.memberships.contains(approvalConfig.risk),
        platformOperations = ldapUser.memberships.contains(approvalConfig.infrastructure)))

  private def decode(token: String, secret: String, algo: JwtHmacAlgorithm): Either[Throwable, Json] =
    JwtCirce.decodeJson(token, secret, Seq(algo)).toEither

  private def encode(json: Json, secret: String, algo: JwtAlgorithm): F[String] =
    Sync[F].delay(JwtCirce.encode(json, secret, algo))

  override def login(username: String, password: String): OptionT[F, Token] =
    for {
      user <- ldapClient.validateUser(username, password)
      token <- OptionT.liftF(refresh(user))
    } yield token

  override def refresh(user: User): F[Token] =
    for {
      accessToken <- encode(user.asJson, restConfig.secret, algo)
      refreshToken <- encode(user.asJson, restConfig.secret, algo)
    } yield Token(accessToken, refreshToken)

  override def validate(token: String): EitherT[F, Throwable, User] = {
    EitherT.fromEither(for {
      maybeToken <- decode(token, restConfig.secret, algo)
      result <- maybeToken.as[User]
    } yield result)
  }

}
