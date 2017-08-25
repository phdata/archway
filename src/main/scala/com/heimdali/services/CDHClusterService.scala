package com.heimdali.services

import javax.inject.Inject

import play.api.Configuration
import play.api.libs.ws.WSClient

import scala.concurrent.Future

class CDHClusterService @Inject()(wsClient: WSClient, configuration: Configuration) extends ClusterService {
  override def list: Future[Seq[Cluster]] = ???
}
