package dropbox4s.datastore.internal.http

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._
import dropbox4s.datastore.auth.AccessToken
import dispatch._, Defaults._

class DatastoreApiRequestorTest extends Specification {
  "get_or_request_url" should {
    "throw exception when both parameter is null" in {
      DatastoreApiRequestor.getOrCreateUrl(null, null) must throwA[IllegalArgumentException]
    }

    "throw exception when dsid parameter is empty string" in {
      DatastoreApiRequestor.getOrCreateUrl("", AccessToken("test-token")) must throwA[IllegalArgumentException]
    }

    "url that is set post parameter dsid and authrization header" in {
      val req = DatastoreApiRequestor.getOrCreateUrl("test-datastore", AccessToken("test-token"))

      req.url must equalTo("https://api.dropbox.com/1/datastores/get_or_create_datastore")
      req.toRequest.getMethod must equalTo("POST")
      req.toRequest.getParams.size() must equalTo(1)
      req.toRequest.getParams.get("dsid").get(0) must equalTo("test-datastore")
      req.toRequest.getHeaders.get("Authorization").get(0) must equalTo("Bearer test-token")
    }
  }
}
