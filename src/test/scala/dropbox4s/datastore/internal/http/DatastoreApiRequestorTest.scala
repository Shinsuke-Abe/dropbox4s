package dropbox4s.datastore.internal.http

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._
import dropbox4s.datastore.auth.AccessToken
import dispatch._
import scala.concurrent.ExecutionException

class DatastoreApiRequestorTest extends Specification {
  val baseUrl = "https://api.dropbox.com/1/datastores"

  val testToken = AccessToken("test-token")

  def authHeaderValue(token: AccessToken) = s"Bearer ${token.token}"

  implicit class RichReq(req: Req) {
    def isDatastoresApi(endpoints: String, method: String, token: AccessToken) = {
      req.url must equalTo(s"${baseUrl}${endpoints}")
      req.toRequest.getMethod must equalTo(method)
      req.toRequest.getHeaders.get("Authorization").get(0) must equalTo(authHeaderValue(token))
    }
  }

  "GerOrCreateRequestor#generateReq" should {
    "throw exception when both parameter is null" in {
      GetOrCreateRequestor.generateReq(null, null) must throwA[IllegalArgumentException]
    }

    "throw exception when dsid parameter is empty string" in {
      GetOrCreateRequestor.generateReq(testToken, "") must throwA[IllegalArgumentException]
    }

    "url that is set post parameter dsid and authorization header" in {
      val req = GetOrCreateRequestor.generateReq(testToken, "test-datastore")

      req isDatastoresApi ("/get_or_create_datastore", "POST", testToken)
      req.toRequest.getParams.size() must equalTo(1)
      req.toRequest.getParams.get("dsid").get(0) must equalTo("test-datastore")
    }
  }

  "GerOrCreateRequestor#apply" should {
    "throw exception when unauth request is failed" in {
      GetOrCreateRequestor(testToken, "failed-request") must throwA[ExecutionException]
    }
  }

  "ListDatastoresRequestor#generateReq" should {
    "throw exception when access token is null" in {
      ListDatastoresUrl.generateReq(null) must throwA[IllegalArgumentException]
    }

    "url that is set authorization header" in {
      val req = ListDatastoresUrl.generateReq(testToken)

      req isDatastoresApi ("/list_datastores", "GET", testToken)
    }
  }

  "ListDatastoresRequestor#apply" should {
    "throw exception when unauth request is failed" in {
      ListDatastoresUrl(testToken) must throwA[ExecutionException]
    }
  }
}
