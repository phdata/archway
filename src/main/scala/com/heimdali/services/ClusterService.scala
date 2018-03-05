package com.heimdali.services

import scala.concurrent.Future

trait ClusterService {
  def list: Future[Seq[Cluster]]
}

case class Impala(server: String)

case class ClusterApps(impala: Impala)

case class Cluster(id: String, name: String, clusterApps: ClusterApps, distribution: CDH, status: String)

sealed trait Distribution {
  def version: String
  def name: String
}

case class CDH(version: String) extends Distribution {
  val name = "CDH"
}