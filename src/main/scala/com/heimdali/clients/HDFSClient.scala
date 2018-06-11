package com.heimdali.clients

import java.io.{File, InputStream, OutputStream}
import java.text.DecimalFormat

import cats.effect.{Async, IO, Sync}
import com.heimdali.services.LoginContextProvider
import com.typesafe.scalalogging.LazyLogging
import org.apache.hadoop.fs.{FileSystem, FileUtil, Path}
import org.apache.hadoop.hdfs.client.HdfsAdmin

case class HDFSAllocation(location: String, maxSizeInGB: Double)

trait HDFSClient[F[_]] {
  def uploadFile(stream: InputStream, hdfsLocation: String): F[Path]

  def createDirectory(location: String, onBehalfOf: Option[String]): F[Path]

  def changeOwner(location: String, user: String): F[Path]

  def setQuota(path: String, maxSizeInGB: Double): F[HDFSAllocation]

  def getQuota(path: Path): F[Double]
}

class HDFSClientImpl[F[_] : Async](fileSystem: () => FileSystem,
                                   hdfsAdmin: () => HdfsAdmin,
                                   loginContextProvider: LoginContextProvider)
  extends HDFSClient[F]
    with LazyLogging {

  implicit def locationToPath(location: String): Path =
    new Path(location)

  def createDirectory(location: String): Path = {
    logger.info(s"Creating $location in ${fileSystem()}")
    fileSystem().mkdirs(location)
    location
  }

  override def createDirectory(location: String, onBehalfOf: Option[String] = None): F[Path] =
    loginContextProvider.elevate(onBehalfOf.getOrElse("hdfs")) { () =>
      createDirectory(location)
    }

  override def changeOwner(location: String, user: String): F[Path] =
    Sync[F].delay {
      FileUtil.setOwner(new File(location), user, user)
      location
    }

  override def setQuota(path: String, maxSizeInGB: Double): F[HDFSAllocation] =
    loginContextProvider.elevate("hdfs") { () =>
      hdfsAdmin().setSpaceQuota(new Path(path), (maxSizeInGB * 1024 * 1024 * 1024).toLong)
      HDFSAllocation(path.toUri.getPath, maxSizeInGB)
    }

  override def getQuota(path: Path): F[Double] =
    Sync[F].delay {
      val dec = new DecimalFormat("0.00")
      dec.format(((fileSystem().getContentSummary(path).getSpaceQuota / 1024.0) / 1024.0) / 1024.0).toDouble
    }

  override def uploadFile(stream: InputStream, hdfsLocation: String): F[Path] =
    Sync[F].delay {
      val path = new Path(hdfsLocation)
      val output: OutputStream = fileSystem().create(path)
      Iterator.continually(stream.read())
        .takeWhile(-1 !=)
        .foreach(output.write)
      output.close()
      path
    }
}