package io.phdata.test

import cats.effect.IO
import cats.implicits._
import io.phdata.services._
import io.circe.Decoder
import io.circe.parser.decode

import scala.collection.JavaConverters._
import scala.io.Source

class TestClusterService extends ClusterService[IO] {

  implicit val location: Decoder[AppLocation] =
    Decoder.instance { a =>
      for {
        host <- a.downField("host").as[String]
        port <- a.downField("port").as[Int]
      } yield AppLocation(host, port)
    }

  case class Services(services: List[ClusterApp])

  implicit val servicesDecoder: Decoder[Services] =
    Decoder.instance { a =>
      val result: List[Decoder.Result[ClusterApp]] = a.keys.get.map { key =>
        for {
          state <- a.downField(key).downField("state").as[String]
          status <- a.downField(key).downField("status").as[String]
          capabilities <- Right(a.downField(key).keys.get.filter {
            k => !k.equalsIgnoreCase("state") && !k.equalsIgnoreCase("status")
          }.map(f => f -> a.downField(key).downField(f).as[List[AppLocation]].toOption.get).toMap)
        } yield ClusterApp(key, key, state, status, capabilities)
      }.toList
      result.sequence.map(Services.apply)
    }

  implicit val decoder: Decoder[Cluster] =
    Decoder.instance { a =>
      for {
        id <- a.downField("id").as[String]
        name <- a.downField("name").as[String]
        cmUrl <- a.downField("cm_url").as[String]
        services <- a.downField("services").as[Services]
        version <- a.downField("distribution").downField("version").as[String]
        status <- a.downField("status").as[String]
      } yield Cluster(id, name, cmUrl, services.services, CDH(version), status)
    }

  override def list: IO[Seq[Cluster]] =
    decode[Seq[Cluster]](
      Source
        .fromResource("rest/cluster.json")
        .bufferedReader()
        .lines()
        .iterator()
        .asScala
        .mkString
    ).toOption.get.pure[IO]
}
