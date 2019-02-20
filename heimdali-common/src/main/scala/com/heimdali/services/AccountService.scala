package com.heimdali.services

import cats.data._
import com.heimdali.models.{Token, User, WorkspaceRequest}

trait AccountService[F[_]] {
  def validate(token: String): EitherT[F, Throwable, User]

  def login(username: String, password: String): OptionT[F, Token]

  def refresh(user: User): F[Token]

  def createWorkspace(user: User): OptionT[F, WorkspaceRequest]

  def getWorkspace(distinguishedName: String): OptionT[F, WorkspaceRequest]
}