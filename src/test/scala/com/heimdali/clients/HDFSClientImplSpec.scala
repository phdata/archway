
package com.heimdali.clients

import java.net.URI

import cats.effect.{Async, IO}
import com.heimdali.services.LoginContextProvider
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.hdfs.MiniDFSCluster
import org.apache.hadoop.hdfs.client.HdfsAdmin
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, Outcome, fixture}

class HDFSClientImplSpec extends fixture.FlatSpec with Matchers with MockitoSugar with BeforeAndAfterAll {

  var cluster: MiniDFSCluster = _

  override protected def beforeAll(): Unit = {
    val configuration = new Configuration()
    cluster = new MiniDFSCluster.Builder(configuration).build()
  }

  override protected def afterAll(): Unit = {
    cluster.shutdown(true)
  }

  behavior of "HDFS Client"

  it should "create a directory on behalf of a user" in { fixture =>
    val context = new TestLoginContext

    val client = new HDFSClientImpl[IO](() => fixture.fileSystem, fixture.admin, context)
    val result = client.createDirectory(fixture.location, Some("jdoe")).unsafeRunSync()

//    verify(context).elevate[IO, Path](ArgumentMatchers.eq("jdoe"))(ArgumentMatchers.any(classOf[() => Path]))(ArgumentMatchers.eq(Async[IO]))
    result.toUri.getPath should be(fixture.location)
    fixture.fileSystem.exists(new Path(fixture.location)) should be(true)
    fixture.fileSystem.delete(new Path(fixture.location), true)
  }

  it should "set quota" in { fixture =>
    val context = new TestLoginContext

    fixture.fileSystem.mkdirs(new Path(fixture.location))

    val client = new HDFSClientImpl[IO](() => fixture.fileSystem, fixture.admin, context)
    val result = client.setQuota(fixture.location, .25).unsafeRunSync()

//    verify(context).elevate[IO, Path](ArgumentMatchers.eq("hdfs"))(ArgumentMatchers.any(classOf[() => Path]))(ArgumentMatchers.eq(Async[IO]))
    result.location should be(fixture.location.toString)
    result.maxSizeInGB should be(.25)
    fixture.fileSystem.delete(new Path(fixture.location), true)
  }

  it should "upload a file" in { fixture =>
    val context = new TestLoginContext
    val data = "test out"
    val dataBytes = data.getBytes
    val filename = s"${fixture.location}/project_a.keytab"
    fixture.fileSystem.mkdirs(new Path(fixture.location))

    val client = new HDFSClientImpl[IO](() => fixture.fileSystem, fixture.admin, context)
    val result = client.uploadFile(new ByteInputStream(dataBytes, dataBytes.length), filename).unsafeRunSync()
    fixture.fileSystem.exists(result) should be(true)
    result.toString should be(filename)
    fixture.fileSystem.delete(new Path(fixture.location), true)
  }

  override def withFixture(test: OneArgTest): Outcome = {
    val configuration = new Configuration()
    val location = "/data/shared_workspaces/project_a"
    val baseUri = new URI(s"hdfs://localhost:${cluster.getNameNodePort}/")
    val fileSystem = FileSystem.get(baseUri, configuration)
    val admin = () => new HdfsAdmin(baseUri, configuration)

    val fixture = FixtureParam(cluster, fileSystem, admin, location)

    withFixture(test.toNoArgTest(fixture))
  }

  case class FixtureParam(cluster: MiniDFSCluster, fileSystem: FileSystem, admin: () => HdfsAdmin, location: String)

}

class TestLoginContext extends LoginContextProvider {

  override def kinit(): IO[Unit] = IO.unit

  override def elevate[F[_] : Async, A](user: String)(block: () => A): F[A] =
    Async[F].delay(block())
}