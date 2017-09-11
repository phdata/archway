package com.heimdali

import be.objectify.deadbolt.scala.cache.HandlerCache
import com.heimdali.repositories.{ProjectRepository, ProjectRepositoryImpl}
import com.heimdali.services._
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}

class HeimdaliModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[HandlerCache].to[HeimdaliCache],
    bind[LDAPClient].to[LDAPClientImpl],
    bind[AccountService].to[LDAPAccountService],
    bind[ProjectService].to[ProjectServiceImpl],
    bind[ProjectRepository].to[ProjectRepositoryImpl],
    bind[ClusterService].to[CDHClusterService],
    bind[Startup].to[HeimdaliStartup].eagerly()
  )

}