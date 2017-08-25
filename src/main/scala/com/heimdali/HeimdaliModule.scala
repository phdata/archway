package com.heimdali

import javax.inject.Inject

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler, DynamicResourceHandler, HandlerKey}
import com.heimdali.repositories.{ProjectRepository, ProjectRepositoryImpl}
import com.heimdali.services._
import play.api.inject.{Binding, Module}
import play.api.libs.json.Json
import play.api.mvc.{Request, Result, Results}
import play.api.{Configuration, Environment}

import scala.concurrent.{ExecutionContext, Future}

class HeimdaliModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[HandlerCache].to[HeimdaliCache],
    bind[LDAPClient].to[LDAPClientImpl],
    bind[AccountService].to[LDAPAccountService],
    bind[ProjectService].to[ProjectServiceImpl],
    bind[ProjectRepository].to[ProjectRepositoryImpl],
    bind[ClusterService].to[CDHClusterService]
  )

}

class HeimdaliHandler(accountService: AccountService)
                     (implicit executionContext: ExecutionContext) extends DeadboltHandler {

  override def beforeAuthCheck[A](request: Request[A]): Future[Option[Result]] =
    Future(None)

  override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] =
    Future(None)

  override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[User]] = {
    val tokenExtractor = """Bearer (.*)""".r
    request.headers.get("Authorization").map {
      case tokenExtractor(token) => accountService.validate(token)
      case _ => Future(None)
    }.getOrElse(Future(None))
  }

  override def onAuthFailure[A](request: AuthenticatedRequest[A]): Future[Result] =
    Future {
      Results.Unauthorized(Json.obj("error" -> "Unauthorized"))
    }

}

class HeimdaliCache @Inject()(accountService: AccountService)
                             (implicit executionContext: ExecutionContext) extends HandlerCache {
  val defaultHandler: DeadboltHandler = new HeimdaliHandler(accountService)

  override def apply(): DeadboltHandler = defaultHandler

  override def apply(v1: HandlerKey): DeadboltHandler = defaultHandler
}