package com.heimdali.clients

import java.io.File
import java.net.URI

import cats.effect.IO
import com.heimdali.itest.fixtures.{IntegrationTest, KerberosTest}
import com.heimdali.services.UGILoginContextProvider
import com.heimdali.itest.fixtures._
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.hdfs.client.HdfsAdmin
import org.apache.hadoop.security.UserGroupInformation
import org.scalatest._
import org.scalatest.mockito.MockitoSugar

class HDFSClientImplIntegrationSpec
    extends FlatSpec
    with Matchers
    with MockitoSugar
    with BeforeAndAfterAll
    with IntegrationTest
    with KerberosTest {

  behavior of "HDFS Client"

  override def beforeAll(): Unit = {
    new Context{
      elevatedFS.mkdirs(new Path(tmpLocation))
    }

  }

  override def afterAll(): Unit = {
    new Context {
      elevatedFS.delete(new Path(tmpLocation), true)
      elevatedFS.delete(new Path(userDirLocation), true)
    }
  }

  it should "create a directory on behalf of a user" in new Context {
    val result =
      client.createDirectory(tmpLocation, Some("hive")).unsafeRunSync()

    result.toUri.getPath shouldBe tmpLocation
    elevatedFS.exists(new Path(tmpLocation)) shouldBe true
    elevatedFS.delete(new Path(tmpLocation), true)
  }

  it should "create a user directory" in new Context {
    val result =
      client.createUserDirectory(testUserName).unsafeRunSync()

    result.toUri.getPath shouldBe userDirLocation
    elevatedFS.exists(new Path(userDirLocation)) shouldBe true
    elevatedFS.getFileStatus(new Path(userDirLocation)).getOwner shouldBe testUserName
    elevatedFS.delete(new Path(userDirLocation), true)
  }

  it should "set quota" in new Context {
    elevatedFS.mkdirs(new Path(tmpLocation))

    val result = client.setQuota(tmpLocation, .25).unsafeRunSync()

    elevatedFS.delete(new Path(tmpLocation), true)

    result.location shouldBe tmpLocation.toString
    result.maxSizeInGB shouldBe .25
  }

  it should "get consumed space" in new Context {
    val elevatedAdmin = context
      .elevate[IO, HdfsAdmin]("hdfs") { () =>
        admin()
      }
      .unsafeRunSync()
    elevatedFS.mkdirs(new Path(tmpLocation))
    elevatedAdmin.setSpaceQuota(new Path(tmpLocation), 1024 * 1024 * 1024)

    val initial = client.getConsumption(tmpLocation).unsafeRunSync()
    elevatedFS.copyFromLocalFile(
      new Path(getClass.getResource("/logo.png").getPath),
      new Path(tmpLocation))
    val after = client.getConsumption(tmpLocation).unsafeRunSync()

    elevatedFS.delete(new Path(tmpLocation), true)

    initial shouldBe 0
    after should be > 0.0
  }

  ignore should "upload a file" in new Context {
    val data = "test out"
    val dataBytes = data.getBytes
    val filename = s"$tmpLocation/project_a.keytab"

    client.createDirectory(tmpLocation, Some("hive")).unsafeRunSync()
    val result = client
      .uploadFile(new ByteInputStream(dataBytes, dataBytes.length), filename)
      .unsafeRunSync()

    elevatedFS.exists(result) shouldBe true
    result.toString shouldBe filename
  }

  trait Context {
    private val fileSystem = () => FileSystem.get(configuration)
    val tmpLocation = "/tmp/heimdali_test_dir"
    val testUserName = "heimdali_test"
    val userDirLocation = s"/user/$testUserName"
    val hdfsUri = new URI(configuration.get("fs.defaultFS"))
    val admin = () => new HdfsAdmin(hdfsUri, configuration)
    val context = new UGILoginContextProvider(itestConfig)

    val elevatedFS = context
      .elevate[IO, FileSystem]("hdfs") { () =>
        fileSystem()
      }
      .unsafeRunSync()

    val client = new HDFSClientImpl[IO](configuration, context)
  }

}
