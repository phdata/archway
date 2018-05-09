package com.heimdali.startup

import akka.actor.ActorSystem
import com.heimdali.services.LoginContextProvider
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait Startup {
  def start(): Future[Unit]
}

class HeimdaliStartup(configuration: Config,
                      dbMigration: DBMigration,
                      loginContextProvider: LoginContextProvider)
                     (implicit executionContext: ExecutionContext,
                      actorSystem: ActorSystem)
  extends Startup with LazyLogging {

  def start(): Future[Unit] = Future {
    val url = configuration.getString("db.meta.url")
    val user = configuration.getString("db.meta.user")
    val pass = configuration.getString("db.meta.password")

    //    dbMigration.migrate(url, user, pass)

    val every = Duration.fromNanos(configuration.getDuration("cluster.sessionRefresh").toNanos)

    actorSystem.scheduler.schedule(0 seconds, every, () => loginContextProvider.kinit())
  }

}