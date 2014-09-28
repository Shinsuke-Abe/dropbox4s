package dropbox4s.datastore.atom

/**
 * @author mao.instantlife at gmail.com
 */

import java.sql.Timestamp

import org.apache.commons.codec.binary.Base64
import org.json4s.JsonDSL._
import org.json4s.JsonAST.JValue
import org.specs2.mutable._

class AtomsTest extends Specification {
  import dropbox4s.datastore.atom.AtomsConverter._

  "WrappedInt" should {
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

    "convert to JValue with toJsonValue" in {
      assertJValueWrappedIntConversion(WrappedInt("9987").toJValue ,("I" -> "9987"))
    }

    def assertJValueWrappedIntConversion(actual: JValue, expected: JValue) = {
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

  "WrappedBytes" should {
    "convert type to byte array with converter" in {
      val testArr = Array[Byte](333.toByte, 444.toByte)

      assertBytesConversion(WrappedBytes(Base64.encodeBase64URLSafeString(testArr)), testArr)
    }

    def assertBytesConversion(actual: Array[Byte], expected: Array[Byte]) = {
      // for test implicit conversion
      actual must equalTo(expected)
    }

    "convert byte array to type with converter" in {
      val testArr = Array[Byte](123.toByte, 222.toByte)

      assertWrappedBytesConversion(testArr, WrappedBytes(Base64.encodeBase64URLSafeString(testArr)))
    }

    def assertWrappedBytesConversion(actual: WrappedBytes, expected: WrappedBytes) = {
      // for test implicit conversion
      actual must equalTo(expected)
    }
  }

  // WrappedSpecial has no implicit conversion.
  // If you set WrappedSpecial to field, use Either[T, WrappedSpecial] type,
  // and set WrappedSpecial value object expressly.
}
