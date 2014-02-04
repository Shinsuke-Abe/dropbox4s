package dropbox4s.datastore

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._
import java.util.Date
import dropbox4s.commons.DropboxException
import dropbox4s.datastore.model.Datastore
import dropbox4s.datastore.internal.jsons.GetOrCreateDatastoreResult

class DatastoresApiTest extends Specification {
  import dropbox4s.datastore.DatastoresApi._

  implicit val token = TestConstants.testUser1
  val createTimeStamp = "%tY%<tm%<td%<tH%<tM%<tS%<tL" format new Date

  val notExistsDs = Datastore("dsnotfound", Some(GetOrCreateDatastoreResult("handlenotfound", 0)))
  val messageNotFound = s"No datastore was found for handle: u'${notExistsDs.handle}'"
  def deleteOkMessage(handle: String) = s"Deleted datastore with handle: u'${handle}'"

  "get" should {
    "throw exception with null value" in {
      get(null) must throwA[IllegalArgumentException]
    }

    "throw exception with length 0 string" in {
      get("") must throwA[IllegalArgumentException]
    }

    "get Datastore result with orCreate flag" in {
      val testDsName = s"test_ds_${createTimeStamp}"
      val createdDs = get(s"$testDsName", orCreate)

      createdDs.dsid must equalTo(s"$testDsName")
      listDatastores.exists(_.dsid == testDsName) must beTrue

      get(s"$testDsName", orCreate).created must beFalse

      // without orCreate flag
      get(s"$testDsName").created must beFalse

      // get snapshots(rev 0, no rows)
      createdDs.snapshot[TestDummyData].rows.isEmpty must beTrue

      // delete datastore
      createdDs.delete.ok must equalTo(deleteOkMessage(createdDs.handle))
      listDatastores.exists(_.dsid == testDsName) must beFalse
    }

    "throw exception not found datastore without orCreate flag" in {
      get("not-found") must throwA[DropboxException](message = "No datastore found for dsid: u'not-found'")
    }
  }

  "delete" should {
    "throw exception not found datastore handle" in {
      notExistsDs.delete must throwA[DropboxException](message = messageNotFound)
    }

    "DsInfo#delete method" in {
      val testDsName = s"fordel_ds_${createTimeStamp}"

      get(s"$testDsName", orCreate)

      val forDeleteDsInfo = listDatastores.find(_.dsid == testDsName).get

      // get snapshots(rev 0, no rows)
      forDeleteDsInfo.snapshot[TestDummyData].rows.isEmpty must beTrue

      forDeleteDsInfo.delete.ok must equalTo(deleteOkMessage(forDeleteDsInfo.handle))
    }
  }

  "snapshot" should {
    "throw exception not found datastore handle" in {
      notExistsDs.snapshot[TestDummyData] must throwA[DropboxException](message = messageNotFound)
    }
  }
}
