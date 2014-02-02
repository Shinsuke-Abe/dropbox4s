package dropbox4s.datastore

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._
import java.util.Date

class DatastoresApiTest extends Specification {
  import dropbox4s.datastore.DatastoresApi._

  implicit val token = TestConstants.testUser1

  "get" should {
    "throw exception with null value" in {
      get(null) must throwA[IllegalArgumentException]
    }

    "throw exception with length 0 string" in {
      get("") must throwA[IllegalArgumentException]
    }

    val createTimeStamp = "%tY%<tm%<td%<tH%<tM%<tS%<tL" format new Date
    val testDsName = s"test_ds_${createTimeStamp}"

    "get Datastore result with orCreate flag" in {
      get(s"$testDsName", orCreate).dsid must equalTo(s"$testDsName")
      listDatastores.exists(_.dsid == testDsName) must beTrue

      get(s"$testDsName", orCreate).created must beFalse

      // without orCreate flag
      get(s"$testDsName").created must beFalse
    }

    // "throw exception not found datastore without orCreate flag"
  }
}
