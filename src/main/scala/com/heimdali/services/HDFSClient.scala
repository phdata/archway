package com.heimdali.services

import java.io.{File, InputStream, OutputStream}
import java.text.DecimalFormat

import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.fs.{FileSystem, FileUtil, Path}
import org.apache.hadoop.hdfs.client.HdfsAdmin

import scala.concurrent.{ExecutionContext, Future}

case class HDFSAllocation(location: String, maxSizeInGB: Double)

trait HDFSClient {
  def uploadFile(stream: InputStream, hdfsLocation: Path): Future[Path]

  def createDirectory(location: String): Path

  def changeOwner(location: String, user: String): Future[Path]

  def setQuota(path: Path, maxSizeInGB: Double): Future[HDFSAllocation]

  def getQuota(path: Path): Future[Double]
}

class HDFSClientImpl(fileSystem: () => FileSystem,
                     hdfsAdmin: HdfsAdmin)
                    (implicit val executionContext: ExecutionContext)
  extends HDFSClient with LazyLogging {

  implicit def locationToPath(location: String): Path =
    new Path(location)

  override def createDirectory(location: String): Path = {
    println(s"Creating $location in ${fileSystem()}")
    fileSystem().mkdirs(location)
    location
  }

  override def changeOwner(location: String, user: String): Future[Path] = Future {
    FileUtil.setOwner(new File(location), user, user)
    location
  }

  override def setQuota(path: Path, maxSizeInGB: Double): Future[HDFSAllocation] = Future {
    hdfsAdmin.setSpaceQuota(path, (maxSizeInGB * 1024 * 1024 * 1024).toLong)
    HDFSAllocation(path.toUri.getPath, maxSizeInGB)
  }

  override def getQuota(path: Path): Future[Double] = Future {
    val dec = new DecimalFormat("0.00")
    dec.format(((fileSystem().getContentSummary(path).getSpaceQuota / 1024.0) / 1024.0) / 1024.0).toDouble
  }

  override def uploadFile(stream: InputStream, hdfsLocation: Path): Future[Path] = Future {
    val output: OutputStream = fileSystem().create(hdfsLocation)
    Iterator.continually(stream.read())
      .takeWhile(-1 !=)
      .foreach(output.write)
    output.close()
    hdfsLocation
  }
}
