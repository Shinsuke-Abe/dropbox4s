package dropbox4s.datastore.internal.http

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._
import dropbox4s.datastore.auth.AccessToken

class DatastoreApiRequestorTest extends Specification {
  "get_or_request_url" should {
    "throw exception when both parameter is null" in {
      DatastoreApiRequestor.getOrCreateUrl(null, null) must throwA[IllegalArgumentException]
    }

    "throw exception when dsid parameter is empty string" in {
      DatastoreApiRequestor.getOrCreateUrl("", AccessToken("test-token")) must throwA[IllegalArgumentException]
    }

    "url that is set post parameter dsid and authrization header" in {
      val url = DatastoreApiRequestor.getOrCreateUrl("test-datastore", AccessToken("test-token"))
    }
  }
}
