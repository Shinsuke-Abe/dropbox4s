package dropbox4s.datastore

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._
import java.util.Date

class DatastoreTest extends Specification {
  import dropbox4s.datastore.DatastoresApi._

  implicit val token = TestConstants.testUser1

  "get_or_create" should {
    "throw exception with null value" in {
      get_or_create(null) must throwA[IllegalArgumentException]
    }

    "throw exception with length 0 string" in {
      get_or_create("") must throwA[IllegalArgumentException]
    }

    "get GetOrCreateResult" in {
      val createTimeStamp = "%tY%<tm%<td%<tH%<tM%<tS%<tL" format new Date
      val testDsName = s"test_ds_${createTimeStamp}"

      get_or_create(s"$testDsName").dsid must equalTo(s"$testDsName")
      list_datastores.exists(_.dsid == testDsName) must beTrue
    }
  }
}
