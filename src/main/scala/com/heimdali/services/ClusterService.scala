package com.heimdali.services

import scala.concurrent.Future

trait ClusterService {
  def list: Future[Seq[Cluster]]
}

sealed trait ClusterApp {
  def id: String
  def name: String
  def status: String
  def state: String
}

case class BasicClusterApp(id: String, name: String, status: String, state: String) extends ClusterApp

case class HostClusterApp(id: String, name: String, status: String, state: String, host: String) extends ClusterApp

case class Cluster(id: String, name: String, clusterApps: Map[String, _ <: ClusterApp], distribution: CDH, status: String)

sealed trait Distribution {
  def version: String
  def name: String
}

case class CDH(version: String) extends Distribution {
  val name = "CDH"
}