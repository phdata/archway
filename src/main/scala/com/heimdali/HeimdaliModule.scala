package com.heimdali

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.models.Subject
import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler, DynamicResourceHandler, HandlerKey}
import com.heimdali.services.{AccountService, LDAPAccountService, LDAPClient, LDAPClientImpl}
import play.api.inject.{Binding, Module}
import play.api.mvc.{Request, Result}
import play.api.{Configuration, Environment}

import scala.concurrent.Future

class HeimdaliModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[HandlerCache].to[HeimdaliCache],
    bind[LDAPClient].to[LDAPClientImpl],
    bind[AccountService].to[LDAPAccountService]
  )

}

class HeimdaliHandler extends DeadboltHandler {
  override def beforeAuthCheck[A](request: Request[A]): Future[Option[Result]] = ???

  override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[Subject]] = ???

  override def onAuthFailure[A](request: AuthenticatedRequest[A]): Future[Result] = ???

  override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] = ???
}

class HeimdaliCache extends HandlerCache {
  val defaultHandler: DeadboltHandler = new HeimdaliHandler

  override def apply(): DeadboltHandler = defaultHandler
  override def apply(v1: HandlerKey): DeadboltHandler = defaultHandler
}