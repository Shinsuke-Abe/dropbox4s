package dropbox4s.datastore.internal

/**
 * @author mao.instantlife at gmail.com
 */

import java.security.MessageDigest
import java.util.Date

import dropbox4s.datastore.TestDummyData
import dropbox4s.datastore.internal.requestparameter.{CreateDatastoreParameter, DataDelete, DataInsert, PutDeltaParameter}
import org.apache.commons.codec.binary.Base64
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.specs2.mutable._

class RequestParameterTest extends Specification {
  implicit val format = DefaultFormats

  implicit val exportJson: TestDummyData => JValue = (data) => ("test" -> data.test)
  val dummyData = TestDummyData("test-data")

  val expectedInsert = JArray(List(JString("I"), JString("test-table"), JString("testrecord"), exportJson(dummyData)))
  val expectedDelete = JArray(List(JString("D"), JString("test-table"), JString("testrecord")))

  "DataInsert#toChangeList" should {
    "create insert case json strings" in {
      DataInsert("test-table", "testrecord", dummyData).toChangeList must equalTo(expectedInsert)
    }
  }
  
  "DataDelete#toChangeList" should {
    "create delete case json strings" in {
      DataDelete("test-table", "testrecord").toChangeList must equalTo(expectedDelete)
    }
  }

  "PutDeltaParameter#changeDeltas" should {
    val changesList =
      PutDeltaParameter("test-handle", 0, None,List(
        DataInsert("test-table", "testrecord", dummyData),
        DataDelete("test-table", "testrecord"))).changeDeltas

    "create list of changes for json value" in {
      changesList must equalTo(List(expectedInsert, expectedDelete))
    }

    "list can convert to Json string" in {
      compact(render(changesList)) must
        equalTo("""[["I","test-table","testrecord",{"test":"test-data"}],["D","test-table","testrecord"]]""")
    }
  }

  "CreateDatastoreParameter#dsid" should {
    val createTimeStamp = "%tY%<tm%<td%<tH%<tM%<tS%<tL" format new Date
    val md = MessageDigest.getInstance("SHA-256")

    "throw IllegalArgumentException if set null value for key" in {
      CreateDatastoreParameter(null) must throwA[IllegalArgumentException]
    }

    "generate dsid from key string" in {
      val testKey = s"dropbox4s-test-shareable-datastore-${createTimeStamp}"

      md.update(testKey.getBytes)

      val expectedDsid = Base64.encodeBase64URLSafeString(md.digest())

      CreateDatastoreParameter(testKey).dsid must equalTo(expectedDsid)
    }
  }
}
