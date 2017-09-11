package com.heimdali

import javax.inject.Inject

import be.objectify.deadbolt.scala.cache.HandlerCache
import be.objectify.deadbolt.scala.{DeadboltHandler, HandlerKey}
import com.heimdali.services.AccountService

import scala.concurrent.ExecutionContext

class HeimdaliCache @Inject()(accountService: AccountService)
                             (implicit executionContext: ExecutionContext) extends HandlerCache {
  val defaultHandler: DeadboltHandler = new HeimdaliHandler(accountService)

  override def apply(): DeadboltHandler = defaultHandler

  override def apply(v1: HandlerKey): DeadboltHandler = defaultHandler
}
