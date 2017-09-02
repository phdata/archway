package com.heimdali.services

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncFlatSpec, FlatSpec, Matchers}
import play.api.libs.json.Json
import play.api.libs.ws.{WSAuthScheme, WSClient, WSRequest, WSResponse}
import play.api.{ConfigLoader, Configuration}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source

class CDHClusterServiceSpec extends AsyncFlatSpec with Matchers with MockitoSugar {

  behavior of "CDH Cluster service"

  it should "return a cluster" in {
    val url = ""
    val name = "odin"
    val version = "5.12.0"

    val username = "admin"
    val password = "admin"

    val configuration = mock[Configuration]
    val clusterConfiguration = mock[Configuration]
    val odinConfiguration = mock[Configuration]

    when(odinConfiguration.get[String](matches("url"))(any[ConfigLoader[String]]))
      .thenReturn(url)
    when(odinConfiguration.get[String](matches("username"))(any[ConfigLoader[String]]))
      .thenReturn(username)
    when(odinConfiguration.get[String](matches("password"))(any[ConfigLoader[String]]))
      .thenReturn(password)

    when(clusterConfiguration.get[Configuration](matches(name))(any[ConfigLoader[Configuration]]))
      .thenReturn(odinConfiguration)
    when(clusterConfiguration.keys)
      .thenReturn(Set(name))

    when(configuration.get[Configuration](matches("clusters"))(any[ConfigLoader[Configuration]]))
      .thenReturn(clusterConfiguration)

    val wsResponse = mock[WSResponse]
    when(wsResponse.json).thenReturn(Json.parse(Source.fromResource("cloudera/cluster.json").getLines().mkString))

    val wsReqeuest = mock[WSRequest]
    when(wsReqeuest.withAuth(username, password, WSAuthScheme.BASIC)).thenReturn(wsReqeuest)
    when(wsReqeuest.get).thenReturn(Future {
      wsResponse
    })

    val cdhClient = mock[WSClient]
    when(cdhClient.url(s"$url/clusters/$name")).thenReturn(wsReqeuest)

    val service = new CDHClusterService(cdhClient, configuration)(ExecutionContext.global)
    service.list map { list =>

      verify(wsReqeuest).withAuth(username, password, WSAuthScheme.BASIC)

      list should have length 1
      list should be(Seq(Cluster(name, "Odin", CDH(version))))
    }
  }
}