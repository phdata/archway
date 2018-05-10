
package com.heimdali.clients

import java.net.URI

import com.heimdali.services.LoginContextProvider
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FSDataOutputStream, FileSystem, Path}
import org.apache.hadoop.hdfs.MiniDFSCluster
import org.apache.hadoop.hdfs.client.HdfsAdmin
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.{FutureOutcome, Matchers, fixture}

import scala.concurrent.Future

class HDFSClientImplSpec extends fixture.AsyncFlatSpec with Matchers with AsyncMockFactory {

  behavior of "HDFS Client"

  it should "create a directory on behalf of a user" in { fixture =>
    val context = mock[LoginContextProvider]
    (context.elevate[Path](_: String)(_: () => Path))
      .expects("jdoe", *)
      .onCall((location, path) => Future(Some(path())))

    val client = new HDFSClientImpl(() => fixture.fileSystem, fixture.admin, context)

    client.createDirectory(fixture.location.toUri.getPath, Some("jdoe")).map { result =>
      result.toUri.getPath should be(fixture.location.toUri.getPath)

      fixture.fileSystem.exists(fixture.location) should be(true)
    }
  }

  it should "set quota" in { fixture =>
    val context = mock[LoginContextProvider]
    (context.elevate[Path](_: String)(_: () => Path))
      .expects("hdfs", *)
      .onCall((location, path) => Future(Some(path())))

    fixture.fileSystem.mkdirs(fixture.location)

    val client = new HDFSClientImpl(() => fixture.fileSystem, fixture.admin, context)
    client.setQuota(fixture.location, .25).map { result =>
      result.location should be(fixture.location.toString)
      result.maxSizeInGB should be(.25)
    }
  }

  it should "upload a file" in { fixture =>
    val context = mock[LoginContextProvider]
    val outputStream = mock[FSDataOutputStream]
    val data = "test out"
    val dataBytes = data.getBytes

    val client = new HDFSClientImpl(() => fixture.fileSystem, fixture.admin, context)
    client.uploadFile(new ByteInputStream(dataBytes, dataBytes.length), fixture.location).map { result =>
      fixture.fileSystem.exists(result) should be(true)
      result.toString should be(fixture.location.toString)
    }

  }

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = {
    val location = "/data/shared_workspaces/project_a"
    val configuration = new Configuration()
    val cluster = new MiniDFSCluster.Builder(configuration).build()
    val baseUri = new URI(s"hdfs://localhost:${cluster.getNameNodePort}/")
    val fileSystem = FileSystem.get(baseUri, configuration)
    val admin = () => new HdfsAdmin(baseUri, configuration)

    val fixture = FixtureParam(cluster, fileSystem, admin, new Path(location))

    withFixture(test.toNoArgAsyncTest(fixture))
  }

  case class FixtureParam(cluster: MiniDFSCluster, fileSystem: FileSystem, admin: () => HdfsAdmin, location: Path)

}