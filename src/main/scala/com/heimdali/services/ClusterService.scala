package com.heimdali.services

import scala.concurrent.Future

trait ClusterService {
  def list: Future[Seq[Cluster]]
}

case class Cluster(id: String, nanme: String, distribution: Distribution)

sealed trait Distribution {
  def name: String

  def version: String
}

case class CDH(version: String) extends Distribution {
  val name = "CDH"
}