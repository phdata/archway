package io.phdata.services

import cats.data._
import io.phdata.config.Password
import io.phdata.models.{DistinguishedName, Token, User, WorkspaceRequest}

trait AccountService[F[_]] {
  def validate(token: String): EitherT[F, Throwable, User]

  def ldapAuth(username: String, password: Password): OptionT[F, Token]

  def spnegoAuth(token: String): F[Either[Throwable, Token]]

  def refresh(user: User): F[Token]

  def createWorkspace(user: User): OptionT[F, WorkspaceRequest]

  def getWorkspace(distinguishedName: DistinguishedName): OptionT[F, WorkspaceRequest]
}
