package com.heimdali.services

import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{AsyncFlatSpec, Matchers}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Results}
import play.api.routing.Router
import play.api.routing.sird._
import play.api.test.WsTestClient
import play.api.{BuiltInComponentsFromContext, ConfigLoader, Configuration}
import play.core.server.Server
import play.filters.HttpFiltersComponents

class ClusterServiceSpec extends AsyncFlatSpec with Matchers with AsyncMockFactory {

  behavior of "CDH Cluster service"

  it should "return a cluster" in {
    withCDHClient { client =>
      val url = "http://example.com"
      val id = "ABC"
      val version = "5.5.3"

      val configuration = mock[Configuration]
      val clusterConfiguration = mock[Configuration]
      val odinConfiguration = mock[Configuration]

      (odinConfiguration.get[String](_: String)(_: ConfigLoader[String]))
        .expects("base-url", *)
        .returning(url)
      (odinConfiguration.get[String](_: String)(_: ConfigLoader[String]))
        .expects("version", *)
        .returning(version)
      (odinConfiguration.get[String](_: String)(_: ConfigLoader[String]))
        .expects("id", *)
        .returning(id)

      (clusterConfiguration.get[Configuration](_: String)(_: ConfigLoader[Configuration]))
        .expects("odin", *)
        .returning(odinConfiguration)

      (configuration.get[Configuration](_: String)(_: ConfigLoader[Configuration]))
        .expects("clusters", *)
        .returning(clusterConfiguration)

      val service = new CDHClusterService(client, configuration)
      service.list map { list =>
        list should have length 1
        list should be(Seq(Cluster(id, "Odin", CDH(version))))
      }
    }
  }


  def withCDHClient[T](block: WSClient => T): T = {
    Server.withApplicationFromContext() { context =>
      new BuiltInComponentsFromContext(context) with HttpFiltersComponents {
        override def router: Router = Router.from {
          case GET(p"/clusters") => Action { _ => Results.Ok.sendResource("cloudera/clusters.json")(fileMimeTypes) }
        }
      }.application
    } { implicit port =>
      WsTestClient.withClient { client =>
        block(client)
      }
    }
  }
}