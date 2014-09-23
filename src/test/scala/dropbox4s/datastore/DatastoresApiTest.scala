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

  implicit val auth = TestConstants.testUser1Auth
  val createTimeStamp = "%tY%<tm%<td%<tH%<tM%<tS%<tL" format new Date

  val notExistsDs = Datastore("dsnotfound", Some(GetOrCreateDatastoreResult("handlenotfound", 0)))
  val messageNotFound = s"No datastore was found for handle: u'${notExistsDs.handle}'"
  def deleteOkMessage(handle: String) = s"Deleted datastore with handle: u'${handle}'"

  val dummyJsonConverter: (TestDummyData) => JValue = (data) => ("test" -> data.test)

  val testRowId = "new-row-id"
  val insertRow = TableRow(testRowId, TestDummyData("test value"))
  val updateRow = TableRow(testRowId, TestDummyData("test new value"))

  "datastore api" should {
    val testDsName = s"test_ds_${createTimeStamp}"
    val createdDs = get(s"$testDsName", orCreate)

    "throw exception with null value" in {
      get(null) must throwA[IllegalArgumentException]
    }

    "throw exception with length 0 string" in {
      get("") must throwA[IllegalArgumentException]
    }

    "get Datastore result with orCreate flag" in {
      createdDs.dsid must equalTo(s"$testDsName")

      val dsList = listDatastores
      dsList.exists(_.dsid == testDsName) must beTrue

      dsList.await.list_datastores.get.exists(_.dsid == testDsName) must beTrue
    }

    "get Datastore result on exists store with orCreate flag" in {
      get(s"$testDsName", orCreate).created must beFalse
    }

    "get Datastore result on exists store without orCreate flag" in {
      get(s"$testDsName").created must beFalse
    }

    "get snapshot result" in {
      val testSnapshot = createdDs.snapshot
      testSnapshot.handle must equalTo(createdDs.handle)
    }

    "get table and operation rows" in {
      def testTable = get(s"${testDsName}").snapshot.table("test-table")(dummyJsonConverter)

      def checkTestTable(row: TableRow[TestDummyData]) = {
        val table = testTable
        table.rows.size must equalTo(1)
        table.get(testRowId) must beSome(row)
      }

      val table = testTable
      table.rows must equalTo(List.empty)

      // insert data
      table.insert(insertRow)
      table.insert(insertRow) must throwA[DropboxException](message = "Conflict")
      // check inserted data
      checkTestTable(insertRow)

      // update data
      testTable.update(testRowId, updateRow.data)
      // check updated data
      checkTestTable(updateRow)

      // delete data by record id
      testTable.delete(testRowId)

      // check deleted data
      testTable.rows must equalTo(List.empty)

      // delete datastore
      createdDs.delete.ok must equalTo(deleteOkMessage(createdDs.handle))
      listDatastores.exists(_.dsid == testDsName) must beFalse
    }

    "throw exception not found datastore without orCreate flag" in {
      get("not-found") must throwA[DropboxException](message = "No datastore found for dsid: u'not-found'")
    }
  }

  "multi rows operation" in {
    val testDsName = s"test_rows_op_${createTimeStamp}"
    val createdDs = get(s"$testDsName", orCreate)

    def testTable = get(s"${testDsName}").snapshot.table("product-table"){data: TestProductRow =>
      ("pid" -> data.pid) ~ ("price" -> data.price) ~ ("colors" -> data.colors)
    }

    // insert multi row
    testTable.insert(
      TableRow("testid01", TestProductRow("0001", 100, List("red", "green", "blue"))),
      TableRow("testid02", TestProductRow("0002", 200, List("black", "white"))),
      TableRow("testid03", TestProductRow("0003", 150, List("yellow", "green"))),
      TableRow("testid04", TestProductRow("0004", 1000, List("black", "red", "purple"))),
      TableRow("testid05", TestProductRow("0005", 500, List("white", "gold"))),
      TableRow("testid06", TestProductRow("0006", 50, List("silver")))
    )

    testTable.rows must have size(6)

    // update multi row
    testTable.update(data => data.copy(price = data.price - 100)){ row => (row.data.price >= 500) }

    testTable.get("testid04") must
      equalTo(Some(TableRow("testid04", TestProductRow("0004", 900, List("black", "red", "purple")))))
    testTable.get("testid05") must
      equalTo(Some(TableRow("testid05", TestProductRow("0005", 400, List("white", "gold")))))

    // delete multi row by condition
    testTable.delete(row => row.data.colors.exists(_ =="green"))

    testTable.rows must have size(4)

    // delete multi row by rowids
    testTable.delete("testid02", "testid04")

    testTable.rows must have size(2)

    // truncate rows
    testTable.truncate

    testTable.rows must be empty

    createdDs.delete.ok must equalTo(deleteOkMessage(createdDs.handle))
    listDatastores.exists(_.dsid == testDsName) must beFalse
  }

  "delete datastore" should {
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

  "shareable datastore api" should {
    "throw exception on creation with null key" in {
      createShareable(null) must throwA[IllegalArgumentException]
    }

    "throw exception on creation with empty key" in {
      createShareable("") must throwA[IllegalArgumentException]
    }
  }
  // TODO shareable datastore
  // TODO shareable datastore create
  // TODO list_datastore has shareable datastore
  // TODO remove shareable datastore
  // TODO check shereable datastore or private datastore
  // TODO set role to shareable datastore
  // TODO check role for insert, update, delete data
  // TODO get shareable datastore snapshot

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
      val notFoundTable = Table[TestDummyData]("handlenotfound", "not-found-table", 0, dummyJsonConverter, List.empty)
      notFoundTable.insert(insertRow) must throwA[DropboxException](message = messageNotFound)
    }
  }
}
