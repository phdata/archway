import org.scalatest.{ FlatSpec, Matchers }
import org.apache.hadoop.security.UserGroupInformation
import cats.effect.IO
import com.heimdali.services.UGILoginContextProvider

class UGILoginContextProviderSpec extends FlatSpec with Matchers {
  behavior of "UGILoginContextProvider"

  it should "log in as another user" in {
    import scala.concurrent.ExecutionContext.Implicits.global

    val provider = new UGILoginContextProvider
    val result = provider.elevate[IO, String]("hdfs"){ () =>
      UserGroupInformation.getCurrentUser.getUserName
    }
    result.unsafeRunSync() shouldBe "hdfs"
  }
}
