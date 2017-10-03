package com.heimdali.services

import javax.inject.Inject

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.hdfs.client.HdfsAdmin

import scala.concurrent.{ExecutionContext, Future}

case class HDFSAllocation(location: String, maxSizeInGB: Double)

trait HDFSClient {
  def createDirectory(location: String): Future[Path]

  def setQuota(path: Path, maxSizeInGB: Double): Future[HDFSAllocation]
}

class HDFSClientImpl @Inject()(fileSystem: FileSystem,
                               hdfsAdmin: HdfsAdmin)
                              (implicit val executionContext: ExecutionContext) extends HDFSClient {
  override def createDirectory(location: String): Future[Path] = Future {
    val path = new Path(location)
    fileSystem.mkdirs(path)
    path
  }

  override def setQuota(path: Path, maxSizeInGB: Double): Future[HDFSAllocation] = Future {
    hdfsAdmin.setSpaceQuota(path, (maxSizeInGB * 1024 * 1024 * 1024).toLong)
    HDFSAllocation(path.toUri.getPath, maxSizeInGB)
  }
}
