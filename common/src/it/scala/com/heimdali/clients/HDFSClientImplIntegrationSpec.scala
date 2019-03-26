
package com.heimdali.clients

import java.net.URI

import cats.effect.IO
import com.heimdali.services.UGILoginContextProvider
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.hdfs.client.HdfsAdmin
import org.apache.hadoop.security.UserGroupInformation
import org.scalatest._
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.ExecutionContext
import scala.io.Source

class HDFSClientImplIntegrationSpec extends FlatSpec with Matchers with MockitoSugar with BeforeAndAfterAll {

  behavior of "HDFS Client"

  it should "create a directory on behalf of a user" in new Context {
    val result = client.createDirectory(userLocation, Some("benny")).unsafeRunSync()

    result.toUri.getPath shouldBe userLocation
    elevatedFS.exists(new Path(userLocation)) shouldBe true
    elevatedFS.delete(new Path(userLocation), true)
  }

  it should "set quota" in new Context {
    elevatedFS.mkdirs(new Path(location))

    val result = client.setQuota(location, .25).unsafeRunSync()

    elevatedFS.delete(new Path(location), true)

    result.location shouldBe location.toString
    result.maxSizeInGB shouldBe .25
  }

  it should "get consumed space" in new Context {
    val elevatedAdmin = context.elevate[IO, HdfsAdmin]("hdfs"){ () => admin() }.unsafeRunSync()
    elevatedFS.mkdirs(new Path(location))
    elevatedAdmin.setSpaceQuota(new Path(location), 1024*1024*1024)

    val initial = client.getConsumption(location).unsafeRunSync()
    elevatedFS.copyFromLocalFile(new Path(getClass.getResource("/logo.png").getPath), new Path(location))
    val after = client.getConsumption(location).unsafeRunSync()

    elevatedFS.delete(new Path(location), true)

    initial shouldBe 0
    after should be > 0.0
  }

  ignore should "upload a file" in new Context {
    val data = "test out"
    val dataBytes = data.getBytes
    val filename = s"$userLocation/project_a.keytab"

    client.createDirectory(userLocation, Some("benny")).unsafeRunSync()
    val result = client.uploadFile(new ByteInputStream(dataBytes, dataBytes.length), filename).unsafeRunSync()

    elevatedFS.delete(new Path(userLocation), true)

    elevatedFS.exists(result) shouldBe true
    result.toString shouldBe filename
  }

  override protected def beforeAll(): Unit = {
    System.setProperty("java.security.krb5.conf", getClass.getResource("/krb5.conf").getPath)
    UserGroupInformation.loginUserFromKeytab("benny@JOTUNN.IO", getClass().getResource("/heimdali.keytab").getPath)
  }

  trait Context {
    val configuration = new Configuration()
    private val fileSystem = () => FileSystem.get(configuration)
    val location = "/test/shared_workspaces/project_a"
    val userLocation = "/user/benny/db"
    val hdfsUri = new URI(configuration.get("fs.defaultFS"))
    val admin = () => new HdfsAdmin(hdfsUri, configuration)
    val context = new UGILoginContextProvider()
    val elevatedFS = context.elevate[IO, FileSystem]("hdfs"){ () => fileSystem() }.unsafeRunSync()

    val client = new HDFSClientImpl[IO](configuration, context)
  }

}