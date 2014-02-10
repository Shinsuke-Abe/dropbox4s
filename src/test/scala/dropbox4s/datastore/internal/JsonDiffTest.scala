package dropbox4s.datastore.internal

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._
import org.json4s._
import org.json4s.native.JsonMethods._

class JsonDiffTest extends Specification {
  "json#diff" should {
    "diff top level value" in {
      (parse( """{"test": "hoge"}""") diff parse( """{"test": "fuga"}""")) must
        equalTo(Diff(JObject(List(("test", JString("fuga")))), JNothing, JNothing))
    }

    "deleted top level value" in {
      val value1 = parse("""{"key1": "value1", "key2": "value2"}""")
      val value2 = parse("""{"key1": "value1"}""")

      (value1 diff value2) must equalTo(Diff(JNothing, JNothing, JObject(List(("key2", JString("value2"))))))
    }

    "add key to top level value" in {
      val value1 = parse("""{"key1": "value1"}""")
      val value2 = parse("""{"key1": "value1", "key2": "value2"}""")

      (value1 diff value2) must equalTo(Diff(JNothing, JObject(List(("key2", JString("value2")))), JNothing))
    }

    "add value to list" in {
      (parse("""{"key1": ["value1", "value2"]}""") diff parse("""{"key1": ["value1", "value2", "value3"]}""")) must
        equalTo(
          Diff(JNothing, JObject(List(("key1", JArray(List(JString("value3")))))), JNothing))
    }

    "delete value from list" in {
      val value1 = parse("""{"key1": ["value1", "value2", "value3"]}""")
      val value2 = parse("""{"key1": ["value1", "value3"]}""")

      // deleted value from value1 is value3
      // change value2 to value3 on list => get index from value2 by "value3"
      (value1 diff value2) must
        equalTo(Diff(JObject(List(("key1", JString("value3")))), JNothing, JObject(List(("key1", JArray(List(JString("value3"))))))))
    }
  }

  "json#merge" should {
    "merge 1json has list" in {
      (parse("""{"test": "hoge"}""") merge parse("""{"test2": ["blue", "red"]}""")) must
        equalTo(JObject(List(("test", JString("hoge")), ("test2", JArray(List(JString("blue"), JString("red")))))))
    }

    "merge 2json has list" in {
      (parse("""{"test": ["hoge"]}""") merge parse("""{"test": ["blue", "red"]}""")) must
        equalTo(JObject(List(("test", JArray(List(JString("hoge"),JString("blue"), JString("red")))))))
    }

    "merge string value and list" in {
      // drop string value...
      (parse("""{"test": "hoge"}""") merge parse("""{"test": ["blue", "red"]}""")) must
        equalTo(JObject(List(("test", JArray(List(JString("blue"), JString("red")))))))
    }
  }
}
