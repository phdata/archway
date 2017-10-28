package com.heimdali.services

import java.io.{InputStream, OutputStream}
import javax.inject.Inject

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.hdfs.client.HdfsAdmin

import scala.concurrent.{ExecutionContext, Future}

case class HDFSAllocation(location: String, maxSizeInGB: Double)

trait HDFSClient {
  def uploadFile(stream: InputStream, hdfsLocation: Path): Future[Path]

  def createDirectory(location: String): Future[Path]

  def setQuota(path: Path, maxSizeInGB: Double): Future[HDFSAllocation]
}

class HDFSClientImpl @Inject()(fileSystem: FileSystem,
                               hdfsAdmin: HdfsAdmin)
                              (implicit val executionContext: ExecutionContext) extends HDFSClient {

  override def createDirectory(location: String): Future[Path] =
    Future {
      val path = new Path(location)
      fileSystem.mkdirs(path)
      path
    }

  override def setQuota(path: Path, maxSizeInGB: Double): Future[HDFSAllocation] = Future {
    hdfsAdmin.setSpaceQuota(path, (maxSizeInGB * 1024 * 1024 * 1024).toLong)
    HDFSAllocation(path.toUri.getPath, maxSizeInGB)
  }

  override def uploadFile(stream: InputStream, hdfsLocation: Path): Future[Path] = Future {
    val output: OutputStream = fileSystem.create(hdfsLocation)
    Iterator.continually(stream.read())
      .takeWhile(-1 !=)
      .foreach(output.write)
    output.close()
    hdfsLocation
  }
}
