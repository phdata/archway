package com.heimdali.clients

import java.io.{File, InputStream, OutputStream}
import java.net.URI
import java.text.DecimalFormat

import cats.effect.{Async, Sync}
import cats.implicits._
import com.heimdali.services.LoginContextProvider
import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, FileUtil, Path}
import org.apache.hadoop.hdfs.client.HdfsAdmin

case class HDFSAllocation(location: String, maxSizeInGB: Double)

trait HDFSClient[F[_]] {
  def uploadFile(stream: InputStream, hdfsLocation: String): F[Path]

  def createDirectory(location: String, onBehalfOf: Option[String]): F[Path]

  def deleteDirectory(location: String): F[Unit]

  def createUserDirectory(userName: String): F[Path]

  def setQuota(path: String, maxSizeInGB: Double): F[HDFSAllocation]

  def getQuota(path: Path): F[Double]

  def getConsumption(location: String): F[Double]

  def removeQuota(path: String): F[Unit]
}

class HDFSClientImpl[F[_] : Async](hadoopConfiguration: Configuration,
                                   loginContextProvider: LoginContextProvider)
  extends HDFSClient[F]
    with LazyLogging {

  lazy val fileSystem: FileSystem =
    FileSystem.get(hadoopConfiguration)

  val hdfsUri = new URI(hadoopConfiguration.get("fs.defaultFS"))

  lazy val hdfsAdmin: HdfsAdmin =
    new HdfsAdmin(hdfsUri, hadoopConfiguration)

  implicit def locationToPath(location: String): Path =
    new Path(location)

  private def createHDFSDirectory(location: String): Path = {
    logger.info(s"Creating $location in $fileSystem")
    fileSystem.mkdirs(location)
    location
  }

  override def createDirectory(location: String, onBehalfOf: Option[String] = None): F[Path] =
    loginContextProvider.elevate(onBehalfOf.getOrElse("hdfs")) { () =>
      createHDFSDirectory(location)
    }

  override def createUserDirectory(userName: String): F[Path] = {
    val path = s"/user/$userName"

    loginContextProvider.elevate("hdfs") { () =>
      if(fileSystem.exists(path)){
          fileSystem.getFileStatus(path).getPath
      } else {
        logger.info(s"Creating user directory with path: $path")

        val createdPath = createHDFSDirectory(path)
        fileSystem.setOwner(createdPath, userName, userName)
        createdPath
      }
    }
  }

  override def deleteDirectory(location: String): F[Unit] =
    loginContextProvider.elevate("hdfs") { () =>
      fileSystem.delete(location)
    }

  override def setQuota(path: String, maxSizeInGB: Double): F[HDFSAllocation] =
    loginContextProvider.elevate("hdfs") { () =>
      hdfsAdmin.setSpaceQuota(new Path(path), (maxSizeInGB * 1024 * 1024 * 1024).toLong)
      HDFSAllocation(path.toUri.getPath, maxSizeInGB)
    }

  override def getQuota(path: Path): F[Double] =
    Sync[F].delay {
      val dec = new DecimalFormat("0.00")
      dec.format(((fileSystem.getContentSummary(path).getSpaceQuota / 1024.0) / 1024.0) / 1024.0).toDouble
    }

  override def uploadFile(stream: InputStream, hdfsLocation: String): F[Path] =
    Sync[F].delay {
      val path = new Path(hdfsLocation)
      val output: OutputStream = fileSystem.create(path)
      Iterator.continually(stream.read())
        .takeWhile(-1 !=)
        .foreach(output.write)
      output.close()
      path
    }

  override def getConsumption(location: String): F[Double] =
    loginContextProvider.elevate("hdfs") { () =>
      ((fileSystem.getContentSummary(new Path(location)).getSpaceConsumed / 1024.0) / 1024.0) / 1024.0
    }

  override def removeQuota(path: String): F[Unit] =
    loginContextProvider.elevate("hdfs") { () =>
      hdfsAdmin.clearQuota(new Path(path))
    }.void
}