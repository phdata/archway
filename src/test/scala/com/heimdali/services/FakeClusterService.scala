package com.heimdali.services

import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class FakeClusterService @Inject()(implicit executionContext: ExecutionContext) extends ClusterService {
  override def list: Future[Seq[Cluster]] = Future { Seq(Cluster("ABC", "Odin", CDH("5.3"))) }
}