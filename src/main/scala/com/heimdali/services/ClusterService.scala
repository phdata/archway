package com.heimdali.services

import scala.concurrent.Future

trait ClusterService {
  def list: Future[Seq[Cluster]]
}

case class Cluster(id: String, name: String, distribution: CDH)

sealed trait Distribution {
  def version: String
  def name: String
}

case class CDH(version: String) extends Distribution {
  val name = "CDH"
}