package dropbox4s.datastore.models

/**
 * @author mao.instantlife at gmail.com
 */

import java.sql.Timestamp

import dropbox4s.datastore.atom.AtomsConverter._
import dropbox4s.datastore.atom.{WrappedTimestamp, WrappedBytes, WrappedInt}
import org.apache.commons.codec.binary.Base64
import org.specs2.mutable._
import dropbox4s.datastore.model.{TableRow, Table}
import org.json4s._
import org.json4s.JsonDSL._
import dropbox4s.commons.DropboxException

class TableTest extends Specification {
  // has primitive values
  case class TestValue(key1: String, key2: Int, key3: Option[List[String]], key4: Option[String])
  val converter: (TestValue) => JValue = {value =>
    ("key1" -> value.key1) ~ ("key2" -> value.key2) ~ ("key3" -> value.key3) ~ ("key4" -> value.key4)
  }

  val testTableRows = List(
    TableRow("row-id-1", TestValue("value1", 0, Some(List.empty), None)),
    TableRow("row-id-2", TestValue("value1-2", 1, Some(List.empty), Some("value4-2"))),
    TableRow("row-id-3", TestValue("value1-3", 2, Some(List("value3-3-1", "value3-3-2")), None))
  )

  val testTable = Table("test-handle", "test-table", 0, converter, testTableRows)

  // has wrapped atom values
  case class TestRowHasAtom(key1: WrappedInt, key2: WrappedBytes, key3: WrappedTimestamp, key4: List[WrappedInt])

  val converterHasAtom: (TestRowHasAtom) => JValue = { value =>
    ("key1" -> value.key1.toJsonValue) ~ ("key2", value.key2.toJsonValue) ~
      ("key3", value.key3.toJsonValue) ~ ("key4", value.key4.map(_.toJsonValue))
  }

  val testTableRowsHasAtom = List(
    TableRow("row-id-1",
      TestRowHasAtom(1000, Array[Byte](123.toByte, 222.toByte), new Timestamp(1111L), List(100, 200))),
    TableRow("row-id-2",
      TestRowHasAtom(2000, Array[Byte](223.toByte, 323.toByte), new Timestamp(2222L), List(200, 300))),
    TableRow("row-id-3",
      TestRowHasAtom(3000, Array[Byte](333.toByte, 343.toByte), new Timestamp(3333L), List(300, 400)))
  )

  val testTAbleHasAtom = Table("test-handle", "test-table-has-value", 0, converterHasAtom, testTableRowsHasAtom)

  // deltas
  private def putDelta(key: String, value: JValue) =
    JObject(List(JField(key, JArray(List(JString("P"), value)))))

  private def deleteDelta(key: String) =
    JObject(List(JField(key, JArray(List(JString("D"))))))

  "rowDiff" should {
    "throw exception with null other value" in {
      testTable.rowDiff("row-id-1", null) must throwA[IllegalArgumentException]
    }

    "throw exception with null row id" in {
      testTable.rowDiff(null, TestValue("value1", 0, Some(List("value1")), None)) must throwA[IllegalArgumentException]
    }

    "throw exception with not exists row-id" in {
      testTable.rowDiff("row-not-found", TestValue("value1", 0, Some(List("value1")), None)) must
        throwA[DropboxException]
    }

    "returns field operation in add list value" in {
      testTable.rowDiff("row-id-1", TestValue("value1", 0, Some(List("value1", "value2")), None)) must
        contain(
          putDelta("key3", JArray(List(JString("value1"), JString("value2"))))
        )
    }

    "returns field operation in change list value" in {
      testTable.rowDiff("row-id-3", TestValue("value1-3", 2, Some(List("value3-3-3", "value3-3-4")), None)) must
        contain(
          putDelta("key3", JArray(List(JString("value3-3-3"), JString("value3-3-4"))))
        )
    }

    "returns field operation in list to empty" in {
      testTable.rowDiff("row-id-3", TestValue("value1-3", 2, Some(List.empty), None)) must
        contain(
          putDelta("key3", JArray(List.empty))
        )
    }

    "returns field operation in delete list" in {
      testTable.rowDiff("row-id-3", TestValue("value1-3", 2, None, None)) must
        contain(deleteDelta("key3"))
    }

    "returns fields operation" in {
      testTable.rowDiff("row-id-2", TestValue("value1-2-test", 1, Some(List("value3-2-1", "value3-2-1")), None)) must
        contain(
          putDelta("key1", JString("value1-2-test")),
          deleteDelta("key4"),
          putDelta("key3", JArray(List(JString("value3-2-1"), JString("value3-2-1"))))
        )
    }

    "returns field operation in change atom int" in {
      testTAbleHasAtom.rowDiff("row-id-1",
        TestRowHasAtom(5000, Array[Byte](123.toByte, 222.toByte), new Timestamp(1111L), List(100, 200))) must
          equalTo(List(putDelta("key1", WrappedInt("5000").toJsonValue)))
    }

    "returns field operation in change atom byte" in {
      testTAbleHasAtom.rowDiff("row-id-2",
        TestRowHasAtom(2000, Array[Byte](444.toByte, 444.toByte), new Timestamp(2222L), List(200, 300))) must
          equalTo(List(putDelta("key2", (Array[Byte](444.toByte, 444.toByte)).toJsonValue)))
    }

    "returns field operation in change atom timestamp" in {
      testTAbleHasAtom.rowDiff("row-id-3",
        TestRowHasAtom(3000, Array[Byte](333.toByte, 343.toByte), new Timestamp(3335L), List(300, 400))) must
          equalTo(List(putDelta("key3", (new Timestamp(3335L)).toJsonValue)))
    }

    "returns field operation in change atom list" in {
      testTAbleHasAtom.rowDiff("row-id-3",
        TestRowHasAtom(3000, Array[Byte](333.toByte, 343.toByte), new Timestamp(3333L), List(400, 500, 600))) must
          equalTo(List(putDelta("key4",
            JArray(List(WrappedInt("400"), WrappedInt("500"), WrappedInt("600")).map(_.toJsonValue)))))
    }
  }
}
