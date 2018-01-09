package com.heimdali.services

import java.io.{InputStream, OutputStream}
import java.text.DecimalFormat

import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.hdfs.client.HdfsAdmin

import scala.concurrent.{ExecutionContext, Future}

case class HDFSAllocation(location: String, maxSizeInGB: Double)

trait HDFSClient {
  def uploadFile(stream: InputStream, hdfsLocation: Path): Future[Path]

  def createDirectory(location: String): Future[Path]

  def setQuota(path: Path, maxSizeInGB: Double): Future[HDFSAllocation]

  def getQuota(path: Path): Future[Double]
}

class HDFSClientImpl(fileSystem: FileSystem,
                     hdfsAdmin: HdfsAdmin)
                    (implicit val executionContext: ExecutionContext)
  extends HDFSClient with LazyLogging {
  logger.info("filesystem looks like {}", fileSystem)

  override def createDirectory(location: String): Future[Path] = Future {
    logger.info("Creating {} in {}", location, fileSystem)
    val path = new Path(location)
    val result = fileSystem.mkdirs(path)
    path
  }

  override def setQuota(path: Path, maxSizeInGB: Double): Future[HDFSAllocation] = Future {
    hdfsAdmin.setSpaceQuota(path, (maxSizeInGB * 1024 * 1024 * 1024).toLong)
    HDFSAllocation(path.toUri.getPath, maxSizeInGB)
  }

  override def getQuota(path: Path): Future[Double] = Future {
    val dec = new DecimalFormat("0.00")
    dec.format(((fileSystem.getContentSummary(path).getSpaceQuota / 1024.0) / 1024.0) / 1024.0).toDouble
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
