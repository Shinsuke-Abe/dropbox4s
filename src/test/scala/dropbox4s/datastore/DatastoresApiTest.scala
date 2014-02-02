package dropbox4s.datastore

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._
import java.util.Date
import dropbox4s.commons.DropboxException
import dropbox4s.datastore.model.Datastore
import dropbox4s.datastore.internal.jsons.GetOrCreateResult

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
      val createdDs = get(s"$testDsName", orCreate)

      createdDs.dsid must equalTo(s"$testDsName")
      listDatastores.exists(_.dsid == testDsName) must beTrue

      get(s"$testDsName", orCreate).created must beFalse

      // without orCreate flag
      get(s"$testDsName").created must beFalse

      // delete datastore
      createdDs.delete.ok must equalTo(s"Deleted datastore with handle: u'${createdDs.handle}'")
      listDatastores.exists(_.dsid == testDsName) must beFalse
    }

    "throw exception not found datastore without orCreate flag" in {
      get("not-found") must throwA[DropboxException](message = "No datastore found for dsid: u'not-found'")
    }
  }

  "delete" should {
    "throw exception not found datastore handle" in {
      Datastore("dsnotfound", Some(GetOrCreateResult("handlenotfound", 0))).delete must
        throwA[DropboxException](message = "No datastore was found for handle: u'handlenotfound'")
    }
  }
}
