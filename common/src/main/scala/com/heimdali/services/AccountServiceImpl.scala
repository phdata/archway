package com.heimdali.services

import java.time.Instant

import cats.data._
import cats.effect._
import cats.implicits._
import com.heimdali.AppContext
import com.heimdali.clients.LDAPUser
import com.heimdali.config.{ApprovalConfig, RestConfig}
import com.heimdali.models._
import com.typesafe.scalalogging.LazyLogging
import io.circe.Json
import io.circe.syntax._
import pdi.jwt.algorithms.JwtHmacAlgorithm
import pdi.jwt.{JwtAlgorithm, JwtCirce}

import scala.concurrent.duration._

class AccountServiceImpl[F[_] : Sync : Timer](context: AppContext[F],
                                              workspaceService: WorkspaceService[F],
                                              templateService: TemplateService[F],
                                              provisionService: ProvisioningService[F])
  extends AccountService[F]
    with LazyLogging {

  val approvalConfig: ApprovalConfig = context.appConfig.approvers
  val restConfig: RestConfig = context.appConfig.rest

  private val algo: JwtAlgorithm.HS512.type = JwtAlgorithm.HS512

  implicit def convertUser(ldapUser: LDAPUser): User = {
    def memberOf(check: ApprovalConfig => Option[String]) =
      check(approvalConfig)
        .map(r => ldapUser.memberships.map(_.toLowerCase()).contains(r.toLowerCase()))
        .getOrElse(false)
    User(ldapUser.name,
      ldapUser.username,
      ldapUser.distinguishedName,
      UserPermissions(
        riskManagement = memberOf(_.risk),
        platformOperations = memberOf(_.infrastructure)
      ))
  }

  private def decode(token: String, secret: String, algo: JwtHmacAlgorithm): Either[Throwable, Json] =
    JwtCirce.decodeJson(token, secret, Seq(algo)).attempt.get

  private def encode(json: Json, secret: String, algo: JwtAlgorithm): F[String] =
    Sync[F].delay(JwtCirce.encode(json, secret, algo))

  override def login(username: String, password: String): OptionT[F, Token] =
    for {
      user <- context.lookupLDAPClient.validateUser(username, password)
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
      result <- EitherT.fromOptionF(context.lookupLDAPClient.findUser(user.distinguishedName).value, new Throwable())
    } yield convertUser(result)
  }

  override def createWorkspace(user: User): OptionT[F, WorkspaceRequest] = {
    OptionT(workspaceService.findByUsername(user.username).value.flatMap {
      case Some(_) => Sync[F].pure(None)
      case None =>
        for {
          template <- templateService.defaults(user)
          time <- Clock[F].realTime(MILLISECONDS)
          workspace <- templateService.workspaceFor(template, "user").map(_.copy(requestDate = Instant.ofEpochMilli(time)))
          savedWorkspace <- workspaceService.create(workspace)
          _ <- provisionService.attemptProvision(savedWorkspace, 0)
          completed <- workspaceService.find(savedWorkspace.id.get).value
        } yield completed
    })
  }

  override def getWorkspace(distinguishedName: String): OptionT[F, WorkspaceRequest] =
    workspaceService.findByUsername(distinguishedName)
}
