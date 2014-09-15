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
