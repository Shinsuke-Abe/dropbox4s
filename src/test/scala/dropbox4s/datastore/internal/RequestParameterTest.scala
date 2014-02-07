package dropbox4s.datastore.internal

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._
import dropbox4s.datastore.internal.requestparameter.{DataDelete, PutDeltaParameter, DataInsert}
import org.json4s._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods._
import dropbox4s.datastore.TestDummyData

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
      PutDeltaParameter("test-handle", 0, None, List(DataInsert("test-table", "testrecord", dummyData))).changeDeltas

    "create list of changes for json value" in {
      changesList must equalTo(List(expectedInsert))
    }

    "list can convert to Json string" in {
      compact(render(changesList)) must
        equalTo("""[["I","test-table","testrecord",{"test":"test-data"}]]""")
    }
  }
}
