package dropbox4s.datastore.atom

/**
 * @author mao.instantlife at gmail.com
 */

import java.sql.Timestamp

import org.json4s._
import org.json4s.native.JsonMethods._
import org.specs2.mutable._

class AtomsTest extends Specification {
  implicit val format = DefaultFormats

  import dropbox4s.datastore.atom.AtomsConverter._

  "WrappedInt" should {
    "parse json value to type" in {
      val testJsonValue = parse("""{"I": "12345"}""")

      testJsonValue.extract[WrappedInt].I.toLong must equalTo(12345l)
    }

    "parse type value to json" in {
      WrappedInt(2345l.toString).toJsonValue must equalTo(parse("""{"I":"2345"}"""))
    }

    "convert type to Int with converter" in {
      assertIntConversion(WrappedInt("3456"), 3456)
    }

    "convert type to Long with converter" in {
      assertLongConversion(WrappedInt("5678"), 5678l)
    }

    def assertIntConversion(actual: Int, expected: Int) = {
      // for test implicit conversion
      actual must equalTo(expected)
    }

    def assertLongConversion(actual: Long, expected: Long) = {
      // for test implicit conversion
      actual must equalTo(expected)
    }

    "convert Int to type with converter" in {
      assertWrappedIntConversion(9987, WrappedInt("9987"))
    }

    "convert Long to type with converter" in {
      assertWrappedIntConversion(8879l, WrappedInt("8879"))
    }

    def assertWrappedIntConversion(actual: WrappedInt, expected: WrappedInt) = {
      // for test implicit conversion
      actual must equalTo(expected)
    }
  }

  "WrappedTimestamp" should {
    "convert type to Timestamp with converter" in {
      assertTimestampConversion(WrappedTimestamp("12345"), new Timestamp(12345L))
    }

    "convert type to Long with converter" in {
      assertLongConversion(WrappedTimestamp("23456"), 23456L)
    }

    def assertTimestampConversion(actual: Timestamp, expected: Timestamp) = {
      // for test implicit conversion
      actual must equalTo(expected)
    }

    def assertLongConversion(actual: Long, expected: Long) = {
      // for test implicit conversion
      actual must equalTo(expected)
    }

    "convert Timestamp to type with converter" in {
      assertWrappedTimestampConversion(new Timestamp(2222L), WrappedTimestamp("2222"))
    }

    "convert Long to type with converter" in {
      assertWrappedTimestampConversion(3333L, WrappedTimestamp("3333"))
    }

    def assertWrappedTimestampConversion(actual: WrappedTimestamp, expected: WrappedTimestamp) = {
      // for test implicit conversion
      actual must equalTo(expected)
    }
  }

  // WrappedSpecial
  // WrappedBytes
  // Dbase64 encode utilities
}
