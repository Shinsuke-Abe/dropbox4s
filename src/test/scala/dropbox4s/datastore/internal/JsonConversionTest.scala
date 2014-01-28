package dropbox4s.datastore.internal

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._
import org.json4s._
import org.json4s.native.JsonMethods._
import dropbox4s.datastore.models.GerOrCreateResult

class JsonConversionTest extends Specification {
  implicit val format = DefaultFormats

  "get_or_create_result" should {
    "json convert to GetOrCreateResult" in {
      val testResult = parse("""{"handle": "1PuUJ3DvMI71OYx1gcqWHzzdva2EpF", "rev": 0, "created": true}""")

      testResult.extract[GerOrCreateResult] must equalTo(GerOrCreateResult("1PuUJ3DvMI71OYx1gcqWHzzdva2EpF", 0, true))
    }
  }
}
