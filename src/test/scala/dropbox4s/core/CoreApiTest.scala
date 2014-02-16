package dropbox4s.core

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._
import org.specs2.mock.Mockito
import com.dropbox.core.{DbxClient, DbxRequestConfig, DbxAccountInfo}
import com.dropbox.core.DbxAccountInfo.Quota
import java.util.Locale

class CoreApiTest extends Specification with Mockito {
  import CoreApi._
  import dropbox4s.datastore.TestConstants._

  val requestConfig = new DbxRequestConfig("TestAppForLibrary/1.0 dropbox4s/0.0.1", Locale.getDefault.toString)

  implicit val mockedDbxClient = new DbxClient(requestConfig, testUser1.token)

  "accountInfo" should {
    "call DbxClient.getAccountInfo" in {
      accountInfo.userId must equalTo(testUser1Id)
    }
  }
}
