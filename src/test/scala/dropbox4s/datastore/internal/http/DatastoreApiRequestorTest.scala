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
import dropbox4s.datastore.internal.requestparameter.{CreateDatastoreParameter, ListAwaitParameter, DataInsert, PutDeltaParameter}
import com.dropbox.core.DbxAuthFinish

class DatastoreApiRequestorTest extends Specification {
  val baseUrl = "https://api.dropbox.com/1/datastores"

  val testAuth = new DbxAuthFinish("test-token", "userId", null)

  def authHeaderValue(auth: DbxAuthFinish) = s"Bearer ${auth.accessToken}"

  implicit class RichReq(req: Req) {
    def isDatastoresApi(endpoints: String, method: String, auth: DbxAuthFinish) = {
      req.url must equalTo(s"${baseUrl}${endpoints}")
      req.toRequest.getMethod must equalTo(method)
      req.toRequest.getHeaders.get("Authorization").get(0) must equalTo(authHeaderValue(auth))
    }
  }

  "DatastoreApiRequestor#request" should {
    "throw exception when unauth request is failed" in {
      GetOrCreateDatastoreRequestor.request(testAuth, "failed-request") must throwA[ExecutionException]
    }
  }

  // datastores/create_datastore
  "CreateDatastoreRequestor#generateReq" should {
    "throw exception when both parameter is null" in {
      CreateDatastoreRequestor.generateReq(null, null) must throwA[IllegalArgumentException]
    }

    "throw exception when input parameter is null value" in {
      CreateDatastoreRequestor.generateReq(testAuth, null) must throwA[IllegalArgumentException]
    }

    "throw exception when input parameter key value is null" in {
      CreateDatastoreRequestor.generateReq(testAuth, CreateDatastoreParameter(null)) must throwA[IllegalArgumentException]
    }

    "url that is set post parameter dsid and key, and authorization header" in {
      val testParam = CreateDatastoreParameter("test-shareable-datastore")
      val req = CreateDatastoreRequestor.generateReq(testAuth, testParam)

      req isDatastoresApi("/create_datastore", "POST", testAuth)
      req.toRequest.getParams.size must equalTo(2)
      req.toRequest.getParams.get("key").get(0) must equalTo(testParam.key)
      req.toRequest.getParams.get("dsid").get(0) must equalTo(testParam.dsid)
    }
  }

  // datastores/get_or_create_datastore
  "GerOrCreateDatastoreRequestor#generateReq" should {
    "throw exception when both parameter is null" in {
      GetOrCreateDatastoreRequestor.generateReq(null, null) must throwA[IllegalArgumentException]
    }

    "throw exception when dsid parameter is empty string" in {
      GetOrCreateDatastoreRequestor.generateReq(testAuth, "") must throwA[IllegalArgumentException]
    }

    "url that is set post parameter dsid and authorization header" in {
      val req = GetOrCreateDatastoreRequestor.generateReq(testAuth, "test-datastore")

      req isDatastoresApi ("/get_or_create_datastore", "POST", testAuth)
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
      GetDatastoreRequestor.generateReq(testAuth, "") must throwA[IllegalArgumentException]
    }

    "url that is set post parameter dsid and authorization header" in {
      val req = GetDatastoreRequestor.generateReq(testAuth, "test-datastore")

      req isDatastoresApi ("/get_datastore", "POST", testAuth)
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
      DeleteDatastoreRequestor.generateReq(testAuth, "") must throwA[IllegalArgumentException]
    }

    "url that is set post parameter dsid and authorization header" in {
      val req = DeleteDatastoreRequestor.generateReq(testAuth, "test-handle")

      req isDatastoresApi ("/delete_datastore", "POST", testAuth)
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
      val req = ListDatastoresRequestor.generateReq(testAuth, null)

      req isDatastoresApi ("/list_datastores", "POST", testAuth)
    }
  }

  // datastore/await for list_datastores result
  "AwaitListDatastoresRequestor#generateReq" should {
    "url that is set get parameter list_datastores and authorization header" in {
      val req = AwaitListDatastoresRequestor.generateReq(testAuth, ListAwaitParameter("test-await-token"))

      req.url must startWith(s"${baseUrl}/await")
      req.toRequest.getMethod must equalTo("GET")
      req.toRequest.getHeaders.get("Authorization").get(0) must equalTo(authHeaderValue(testAuth))
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
      GetSnapshotRequestor.generateReq(testAuth, "") must throwA[IllegalArgumentException]
    }

    "url that is set post parameter handle and authorization header" in {
      val req = GetSnapshotRequestor.generateReq(testAuth, "test-handle")

      req isDatastoresApi ("/get_snapshot", "POST", testAuth)
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
      PutDeltaRequestor.generateReq(testAuth, null) must throwA[IllegalArgumentException]
    }

    "throw exception when handle of change list parameter is null" in {
      PutDeltaRequestor.generateReq(testAuth, PutDeltaParameter(null, 0, None, List(insert))) must
      throwA[IllegalArgumentException]
    }

    "throw exception when handle of change list parameter is empty string" in {
      PutDeltaRequestor.generateReq(testAuth, PutDeltaParameter("", 0, None, List(insert))) must
      throwA[IllegalArgumentException]
    }

    "throw exception when change list is null" in {
      PutDeltaRequestor.generateReq(testAuth, PutDeltaParameter("test-handle", 0, None, null)) must
      throwA[IllegalArgumentException]
    }

    "throw exception when change list is null" in {
      PutDeltaRequestor.generateReq(testAuth, PutDeltaParameter("test-handle", 0, None, List.empty)) must
      throwA[IllegalArgumentException]
    }

    "url that is set post parameter handle and authorization header" in {
      val req = PutDeltaRequestor.generateReq(testAuth, params)

      req isDatastoresApi ("/put_delta", "POST", testAuth)
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
