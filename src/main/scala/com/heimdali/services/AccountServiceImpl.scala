package com.heimdali.services

import java.time.Clock

import cats.data._
import cats.effect._
import cats.implicits._
import com.heimdali.clients.{LDAPClient, LDAPUser}
import com.heimdali.config.{ApprovalConfig, RestConfig, WorkspaceConfig}
import com.heimdali.models.{Token, User, UserPermissions, WorkspaceRequest}
import com.typesafe.scalalogging.LazyLogging
import io.circe.Json
import io.circe.syntax._
import pdi.jwt.algorithms.JwtHmacAlgorithm
import pdi.jwt.{JwtAlgorithm, JwtCirce}

class AccountServiceImpl[F[_] : Sync](ldapClient: LDAPClient[F],
                                      restConfig: RestConfig,
                                      approvalConfig: ApprovalConfig,
                                      workspaceConfig: WorkspaceConfig,
                                      workspaceService: WorkspaceService[F])
                                      (implicit val clock: Clock)
  extends AccountService[F]
    with LazyLogging {

  private val algo: JwtAlgorithm.HS512.type = JwtAlgorithm.HS512

  implicit def convertUser(ldapUser: LDAPUser): User = {
    User(ldapUser.name,
      ldapUser.username,
      ldapUser.distinguishedName,
         UserPermissions(riskManagement =
                           ldapUser
                             .memberships
                             .map(_.toLowerCase())
                           .contains(approvalConfig.risk.toLowerCase()),
                         platformOperations =
                           ldapUser
                             .memberships
                             .map(_.toLowerCase())
                             .contains(approvalConfig.infrastructure.toLowerCase())))
  }

  private def decode(token: String, secret: String, algo: JwtHmacAlgorithm): Either[Throwable, Json] =
    JwtCirce.decodeJson(token, secret, Seq(algo)).attempt.get

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
    for {
      maybeToken <- EitherT.fromEither[F](decode(token, restConfig.secret, algo))
      user <- EitherT.fromEither[F](maybeToken.as[User])
      result <- EitherT.fromOptionF(ldapClient.findUser(user.distinguishedName).value, new Throwable())
    } yield convertUser(result)
  }

  override def createWorkspace(user: User): OptionT[F, WorkspaceRequest] = {
    import Generator._
    OptionT(workspaceService.findByUsername(user.username).value.flatMap {
      case Some(_) => Sync[F].pure(None)
      case None =>
        val workspace = Generator[UserTemplate].defaults(user).generate().copy(requestDate = clock.instant())
        for {
          savedWorkspace <- workspaceService.create(workspace)
          _ <- workspaceService.provision(savedWorkspace)
          completed <- workspaceService.find(savedWorkspace.id.get).value
        } yield completed
    })
  }

  override def getWorkspace(distinguishedName: String): OptionT[F, WorkspaceRequest] =
    workspaceService.findByUsername(distinguishedName)
}
