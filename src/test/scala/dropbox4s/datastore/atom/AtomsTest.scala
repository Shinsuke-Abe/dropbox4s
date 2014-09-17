package dropbox4s.datastore.atom

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._
import org.json4s._
import org.json4s.native.JsonMethods._

class AtomsTest extends Specification {
  implicit val format = DefaultFormats

  "WrappedInt" should {
    "parse json value to type" in {
      val testJsonValue = parse("""{"I": "12345"}""")

      testJsonValue.extract[WrappedInt].I.toLong must equalTo(12345l)
    }

    "parse type value to json" in {
      WrappedInt(2345l.toString).toJsonValue must equalTo(parse("""{"I":"2345"}"""))
    }

    import AtomsConverter._
    "convert type to Int with converter" in {
      assertIntConversion(WrappedInt("3456"), 3456)
    }

    "convert type to Long with converter" in {
      assertLongConversion(WrappedInt("5678"), 5678l)
    }

    def assertIntConversion(actual: Int, expected: Int) = {
      actual must equalTo(expected)
    }

    def assertLongConversion(actual: Long, expected: Long) = {
      actual must equalTo(expected)
    }
  }

  // WrappedSpecial
  // WrappedTimestamp(implemented, but not satisfied specification)
  // WrappedBytes
  // Dbase64 encode utilities
  // AtomsUtility
  // Int -> WrappedInt
  // Long -> WrapedInt
  // Short -> WrappedInt
}
