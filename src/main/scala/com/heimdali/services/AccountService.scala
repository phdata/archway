package com.heimdali.services

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.heimdali.clients.LDAPClient
import com.heimdali.models.UserWorkspace
import com.heimdali.provisioning.WorkspaceProvisioner.Start
import com.heimdali.repositories.UserWorkspaceRepository
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import io.circe.Json
import scala.concurrent.duration._
import pdi.jwt.{JwtAlgorithm, JwtCirce}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class User(name: String, username: String)

case class Token(accessToken: String, refreshToken: String)

trait AccountService {
  def validate(token: String): Future[Option[User]]

  def login(username: String, password: String): Future[Option[Token]]

  def refresh(user: User): Future[Token]

  def createWorkspace(username: String): Future[UserWorkspace]

  def findWorkspace(username: String): Future[Option[UserWorkspace]]
}

class AccountServiceImpl(ldapClient: LDAPClient,
                         accountRepo: UserWorkspaceRepository,
                         configuration: Config,
                         provisionFactory: UserWorkspace => ActorRef)
                        (implicit val executionContext: ExecutionContext)
  extends AccountService with LazyLogging {

  val algo: JwtAlgorithm.HS512.type = JwtAlgorithm.HS512
  lazy val secret: String = configuration.getString("rest.secret")

  override def login(username: String, password: String): Future[Option[Token]] = {
    logger.info(s"logging in $username")
    ldapClient.validateUser(username, password) flatMap {
      case Some(user) =>
        logger.info(s"found $user")
        refresh(User(user.name, user.username)).map(Some(_))
      case _ =>
        logger.info(s"no user found for $username using $password")
        Future(None)
    }
  }

  override def refresh(user: User): Future[Token] = Future {
    val accessJson = Json.obj(
      "name" -> Json.fromString(user.name),
      "username" -> Json.fromString(user.username)
    )

    val refreshJson = Json.obj(
      "username" -> Json.fromString(user.username)
    )

    try {
      val accessToken = JwtCirce.encode(accessJson, secret, algo)
      val refreshToken = JwtCirce.encode(refreshJson, secret, algo)

      logger.info(s"generated new token for $user")

      Token(accessToken, refreshToken)
    } catch {
      case exc: Throwable =>
        exc.printStackTrace()
        throw exc
    }
  }

  override def validate(token: String): Future[Option[User]] = Future {
    import io.circe.generic.auto._
    JwtCirce.decodeJson(token, secret, Seq(algo)) match {
      case Success(json) =>
        val Right(user) = json.as[User]
        logger.info(s"validated ${user.username}")
        Some(user)
      case Failure(_) =>
        logger.info(s"no user found for $token")
        None
    }
  }

  override def findWorkspace(username: String): Future[Option[UserWorkspace]] =
    accountRepo.findUser(username)

  implicit val timeout = Timeout(1 second)

  override def createWorkspace(username: String): Future[UserWorkspace] =
    accountRepo.create(username).map { workspace =>
      provisionFactory(workspace) ! Start
      workspace
    }
}