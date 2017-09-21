package com.heimdali

import akka.routing.RoundRobinPool
import be.objectify.deadbolt.scala.cache.HandlerCache
import com.google.inject.AbstractModule
import com.heimdali.actors.{LDAPActor, ProjectProvisioner, ProjectSaver}
import com.heimdali.repositories.{ProjectRepository, ProjectRepositoryImpl}
import com.heimdali.services._
import play.api.libs.concurrent.AkkaGuiceSupport

class HeimdaliModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bind(classOf[HandlerCache]).to(classOf[HeimdaliCache])
    bind(classOf[LDAPClient]).to(classOf[LDAPClientImpl])
    bind(classOf[AccountService]).to(classOf[LDAPAccountService])
    bind(classOf[ProjectService]).to(classOf[ProjectServiceImpl])
    bind(classOf[ProjectRepository]).to(classOf[ProjectRepositoryImpl])
    bind(classOf[ClusterService]).to(classOf[CDHClusterService])
    bind(classOf[Startup]).to(classOf[HeimdaliStartup]).asEagerSingleton()
    bindActor[LDAPActor]("ldap-actor")
    bindActor[ProjectSaver]("project-saver")
    bindActor[ProjectProvisioner]("provisioning-actor", RoundRobinPool(5).props)
  }

}