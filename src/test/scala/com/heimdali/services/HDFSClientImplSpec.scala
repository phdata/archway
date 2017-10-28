package com.heimdali.services

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream
import org.apache.hadoop.fs.{FSDataOutputStream, FileSystem, Path}
import org.apache.hadoop.hdfs.client.HdfsAdmin
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import play.api.Configuration

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class HDFSClientImplSpec extends FlatSpec with Matchers with MockitoSugar {

  behavior of "HDFS Client"

  it should "create a directory" in new Context {
    val result = Await.result(client.createDirectory(location), Duration.Inf)
    verify(fileSystem).mkdirs(path)
    result.toUri.getPath should be(location)
  }

  it should "set permissions" in new Context {
    val result = Await.result(client.setQuota(path, 10), Duration.Inf)
    val bytes = 10L * 1024L * 1024L * 1024L
    verify(admin).setSpaceQuota(path, bytes)
    result.location should be (location)
    result.maxSizeInGB should be (10)
  }

  it should "upload a file" in new Context {
    val outputStream = mock[FSDataOutputStream]
    val data = "test out"
    val dataBytes = data.getBytes

    when(fileSystem.create(any[Path])).thenReturn(outputStream)

    val result = Await.result(client.uploadFile(new ByteInputStream(dataBytes, dataBytes.length), path), Duration.Inf)

    verify(fileSystem).create(path)
    verify(outputStream).close()
    verify(outputStream, times(dataBytes.length)).write(any)

    result.toString should be (path.toString)
  }

  trait Context {
    val location = "/projects/something"
    val path = new Path(location)
    val fileSystem = mock[FileSystem]
    val admin = mock[HdfsAdmin]

    val client = new HDFSClientImpl(fileSystem, admin)
  }

}
