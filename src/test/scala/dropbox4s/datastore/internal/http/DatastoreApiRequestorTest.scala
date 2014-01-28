package dropbox4s.datastore.internal.http

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._

class DatastoreApiRequestorTest extends Specification {
  "get_or_request_url" should {
    "throw exception when both parameter is null" in {
      DatastoreApiRequestor.getOrCreateUrl(null, null) must throwA[IllegalArgumentException]
    }
  }
}
