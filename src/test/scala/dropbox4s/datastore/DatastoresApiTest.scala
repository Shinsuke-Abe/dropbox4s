package dropbox4s.datastore

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._
import java.util.Date
import dropbox4s.datastore.model.{Table, TableRow, Snapshot, Datastore}
import org.json4s.native.JsonMethods._
import org.json4s._
import org.json4s.JsonDSL._
import dropbox4s.datastore.internal.jsonresponse.SnapshotResult
import dropbox4s.commons.DropboxException
import dropbox4s.datastore.internal.jsonresponse.GetOrCreateDatastoreResult
import scala.Some

class DatastoresApiTest extends Specification {
  import dropbox4s.datastore.DatastoresApi._

  implicit val token = TestConstants.testUser1
  val createTimeStamp = "%tY%<tm%<td%<tH%<tM%<tS%<tL" format new Date

  val notExistsDs = Datastore("dsnotfound", Some(GetOrCreateDatastoreResult("handlenotfound", 0)))
  val messageNotFound = s"No datastore was found for handle: u'${notExistsDs.handle}'"
  def deleteOkMessage(handle: String) = s"Deleted datastore with handle: u'${handle}'"

  val dummyJsonGenerator: (TestDummyData) => JValue = (data) => ("test" -> data.test)
  val insertRow = TableRow("new-row-id", TestDummyData("test value"))

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
      val testSnapshot = createdDs.snapshot
      testSnapshot.handle must equalTo(createdDs.handle)
      testSnapshot.tableNames must equalTo(List.empty)

      val table = testSnapshot.table("test-table")(dummyJsonGenerator)

      // insert data
      table.insert(insertRow)
      table.insert(insertRow) must throwA[DropboxException](message = "Conflict")

      // check inserted data
      val insertedSnapshot = get(s"${testDsName}").snapshot
      insertedSnapshot.tableNames must equalTo(List("test-table"))

      val insertedTable = insertedSnapshot.table("test-table")(dummyJsonGenerator)
      insertedTable.rows must equalTo(List(insertRow))

      // delete data by record id
      insertedTable.delete("new-row-id")

      // check deleted data
      val deletedSnapshot = get(s"${testDsName}").snapshot
      val deletedTable = deletedSnapshot.table("test-table")(dummyJsonGenerator)
      deletedTable.rows must equalTo(List.empty)

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
      forDeleteDsInfo.snapshot.handle must equalTo(forDeleteDsInfo.handle)

      forDeleteDsInfo.delete.ok must equalTo(deleteOkMessage(forDeleteDsInfo.handle))
    }
  }

  "snapshot" should {
    "throw exception not found datastore handle" in {
      notExistsDs.snapshot must throwA[DropboxException](message = messageNotFound)
    }
  }

  "Snapshot#table" should {
    "get table rows list with row data type" in {
      implicit val format = DefaultFormats

      val testResult = parse(
        """
          | {
          |   "rev": 0,
          |   "rows": [
          |     {"tid": "default", "rowid": "1", "data": {"test": "testvalue1"}},
          |     {"tid": "default", "rowid": "2", "data": {"test": "testvalue2"}},
          |     {"tid": "nottarget", "rowid": "1", "data": {"key1": "testvalue1", "key2": 2}}
          |   ]
          | }
          | """.stripMargin).extract[SnapshotResult]

      val testTable = Snapshot("test-handle", testResult).table("default") { data: TestDummyData =>
        ("test" -> data.test)
      }

      testTable.handle must equalTo("test-handle")
      testTable.tid must equalTo("default")
      testTable.rev must equalTo(0)
      testTable.rows.size must equalTo(2)
    }
  }

  "insert" should {
    "throw exception not found datastore handle" in {
      val notFoundTable = Table[TestDummyData]("handlenotfound", "not-found-table", 0, dummyJsonGenerator, List.empty)
      notFoundTable.insert(insertRow) must throwA[DropboxException](message = messageNotFound)
    }
  }
}
