package dropbox4s.datastore.internal.http

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._
import scala.concurrent.ExecutionException
import org.json4s._
import org.json4s.JsonDSL._
import dropbox4s.datastore.TestDummyData
import dispatch.Req
import dropbox4s.datastore.internal.requestparameter.{ListAwaitParameter, DataInsert, PutDeltaParameter}
import dropbox4s.datastore.auth.AccessToken

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

  "DatastoreApiRequestor#request" should {
    "throw exception when unauth request is failed" in {
      GetOrCreateDatastoreRequestor.request(testToken, "failed-request") must throwA[ExecutionException]
    }
  }

  // datastores/get_or_create_datastore
  "GerOrCreateDatastoreRequestor#generateReq" should {
    "throw exception when both parameter is null" in {
      GetOrCreateDatastoreRequestor.generateReq(null, null) must throwA[IllegalArgumentException]
    }

    "throw exception when dsid parameter is empty string" in {
      GetOrCreateDatastoreRequestor.generateReq(testToken, "") must throwA[IllegalArgumentException]
    }

    "url that is set post parameter dsid and authorization header" in {
      val req = GetOrCreateDatastoreRequestor.generateReq(testToken, "test-datastore")

      req isDatastoresApi ("/get_or_create_datastore", "POST", testToken)
      req.toRequest.getParams.size() must equalTo(1)
      req.toRequest.getParams.get("dsid").get(0) must equalTo("test-datastore")
    }
  }

  // datastores/get_datastore
  "GerDatastoreRequestor#generateReq" should {
    "throw exception when both parameter is null" in {
      GetDatastoreRequestor.generateReq(null, null) must throwA[IllegalArgumentException]
    }

    "throw exception when dsid parameter is empty string" in {
      GetDatastoreRequestor.generateReq(testToken, "") must throwA[IllegalArgumentException]
    }

    "url that is set post parameter dsid and authorization header" in {
      val req = GetDatastoreRequestor.generateReq(testToken, "test-datastore")

      req isDatastoresApi ("/get_datastore", "POST", testToken)
      req.toRequest.getParams.size() must equalTo(1)
      req.toRequest.getParams.get("dsid").get(0) must equalTo("test-datastore")
    }
  }

  // datastores/delete_datastore
  "DeleteDatastoreRequestor#generateReq" should {
    "throw exception when both parameter is null" in {
      DeleteDatastoreRequestor.generateReq(null, null) must throwA[IllegalArgumentException]
    }

    "throw exception when dsid parameter is empty string" in {
      DeleteDatastoreRequestor.generateReq(testToken, "") must throwA[IllegalArgumentException]
    }

    "url that is set post parameter dsid and authorization header" in {
      val req = DeleteDatastoreRequestor.generateReq(testToken, "test-handle")

      req isDatastoresApi ("/delete_datastore", "POST", testToken)
      req.toRequest.getParams.size() must equalTo(1)
      req.toRequest.getParams.get("handle").get(0) must equalTo("test-handle")
    }
  }

  // datastore/list_datastores
  "ListDatastoresRequestor#generateReq" should {
    "throw exception when access token is null" in {
      ListDatastoresRequestor.generateReq(null, null) must throwA[IllegalArgumentException]
    }

    "url that is set authorization header" in {
      val req = ListDatastoresRequestor.generateReq(testToken, null)

      req isDatastoresApi ("/list_datastores", "POST", testToken)
    }
  }

  // datastore/await for list_datastores result
  "AwaitListDatastoresRequestor#generateReq" should {
    "url that is set get parameter list_datastores and authorization header" in {
      val req = AwaitListDatastoresRequestor.generateReq(testToken, ListAwaitParameter("test-await-token"))

      req.url must startWith(s"${baseUrl}/await")
      req.toRequest.getMethod must equalTo("GET")
      req.toRequest.getHeaders.get("Authorization").get(0) must equalTo(authHeaderValue(testToken))
      req.toRequest.getQueryParams.size must equalTo(1)
      req.toRequest.getQueryParams.get("list_datastores").get(0) must equalTo("""{"token":"test-await-token"}""")
    }
  }

  // datastore/get_snapshot
  "GetSnapshotRequestor#generateReq" should {
    "throw exception when both parameter is null" in {
      GetSnapshotRequestor.generateReq(null, null) must throwA[IllegalArgumentException]
    }

    "throw exception when handle parameter is empty string" in {
      GetSnapshotRequestor.generateReq(testToken, "") must throwA[IllegalArgumentException]
    }

    "url that is set post parameter handle and authorization header" in {
      val req = GetSnapshotRequestor.generateReq(testToken, "test-handle")

      req isDatastoresApi ("/get_snapshot", "POST", testToken)
      req.toRequest.getParams.size() must equalTo(1)
      req.toRequest.getParams.get("handle").get(0) must equalTo("test-handle")
    }
  }

  // datastore/put_delta
  "PutDeltaRequestor#generateReq" should {
    implicit val exportJson: TestDummyData => JValue = (data) => ("test" -> data.test)
    val dummyData = TestDummyData("test-data")

    val insert = DataInsert("test-table", "testrecord", dummyData)
    val params = PutDeltaParameter("test-handle", 0, None, List(insert))

    "throw exception when both parameter is null" in {
      PutDeltaRequestor.generateReq(null, null) must throwA[IllegalArgumentException]
    }

    "throw exception when change list parameter is null" in {
      PutDeltaRequestor.generateReq(testToken, null) must throwA[IllegalArgumentException]
    }

    "throw exception when handle of change list parameter is null" in {
      PutDeltaRequestor.generateReq(testToken, PutDeltaParameter(null, 0, None, List(insert))) must
      throwA[IllegalArgumentException]
    }

    "throw exception when handle of change list parameter is empty string" in {
      PutDeltaRequestor.generateReq(testToken, PutDeltaParameter("", 0, None, List(insert))) must
      throwA[IllegalArgumentException]
    }

    "throw exception when change list is null" in {
      PutDeltaRequestor.generateReq(testToken, PutDeltaParameter("test-handle", 0, None, null)) must
      throwA[IllegalArgumentException]
    }

    "throw exception when change list is null" in {
      PutDeltaRequestor.generateReq(testToken, PutDeltaParameter("test-handle", 0, None, List.empty)) must
      throwA[IllegalArgumentException]
    }

    "url that is set post parameter handle and authorization header" in {
      val req = PutDeltaRequestor.generateReq(testToken, params)

      req isDatastoresApi ("/put_delta", "POST", testToken)
      req.toRequest.getParams.size() must equalTo(3)
      req.toRequest.getParams.get("handle").get(0) must equalTo("test-handle")
      req.toRequest.getParams.get("rev").get(0) must equalTo("0")
      req.toRequest.getParams.get("changes").get(0) must equalTo("""[["I","test-table","testrecord",{"test":"test-data"}]]""")
    }
  }

  // the follow test is specification test.
//  "RequestNotFound" should {
//    "throw exception not found url" in {
//      val notFound = new DatastoreApiRequestor[Unit, Unit] {
//        def apply(token: AccessToken, input: Unit = ()): Unit = executeReq(token, input)
//
//        private[dropbox4s] def generateReq(token: AccessToken, input: Unit = ()) = {
//          require(Option(token).isDefined)
//
//          baseUrl / "not_found" <:< authHeader(token)
//        }
//      }
//
//      notFound(testToken) must throwA[ExecutionException]
//    }
//  }
}
