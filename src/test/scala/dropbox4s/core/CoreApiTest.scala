package dropbox4s.core

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._
import java.util.Locale

class CoreApiTest extends Specification with CoreApi {
  val locale = Locale.getDefault
  val version = "1.0"
  val applicationName = "TestAppForLibrary"

  import dropbox4s.datastore.TestConstants._

  implicit val token = testUser1

  "accountInfo" should {
    "call DbxClient.getAccountInfo" in {
      accountInfo.userId must equalTo(testUser1Id)
    }
  }

}
