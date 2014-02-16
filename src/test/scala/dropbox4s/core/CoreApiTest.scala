package dropbox4s.core

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._
import org.specs2.mock.Mockito
import com.dropbox.core.DbxAccountInfo
import com.dropbox.core.DbxAccountInfo.Quota

class CoreApiTest extends Specification with Mockito {
  import CoreApi._

  val testAccountInfo =
    new DbxAccountInfo(1L, "test-display-name", "test-country", "referralLink", new Quota(1L, 1L, 1L))

  implicit val mockedDbxClient = mock[com.dropbox.core.DbxClient]

  mockedDbxClient.getAccountInfo returns testAccountInfo

  "accountInfo" should {
    "call DbxClient.getAccountInfo" in {
      accountInfo must equalTo(testAccountInfo)
    }
  }
}
