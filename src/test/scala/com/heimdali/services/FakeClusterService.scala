package com.heimdali.services

import javax.inject.Inject

import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

class FakeClusterService @Inject()(configuration: Configuration)
                                  (implicit executionContext: ExecutionContext)
  extends CDHClusterService(null, configuration) {
  override def clusterDetails(baseUrl: String, username: String, password: String) =
    Future {
      Cluster(username, username, FakeClusterService.cdh)
    }
}

object FakeClusterService {
  val cdh = CDH("1.0")
}