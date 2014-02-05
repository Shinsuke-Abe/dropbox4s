package dropbox4s.datastore.internal.http

/*
 * Copyright (C) 2014 Shinsuke Abe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import dropbox4s.datastore.auth.AccessToken
import dispatch._, Defaults._
import dropbox4s.datastore.internal.jsonresponse.{SnapshotResult, DeleteDatastoreResult, GetOrCreateDatastoreResult, ListDatastoresResult}
import org.json4s._
import org.json4s.native.JsonMethods._
import dropbox4s.commons.DropboxException
import org.json4s.JValue

/**
 * @author mao.instantlife at gmail.com
 */
trait DatastoreApiRequestor[ParamType, ResType] {
  implicit val format = DefaultFormats

  val endpoint: String

  protected val notRequired = true

  protected val noParams = Map.empty[String, String]

  lazy val baseUrl = host("api.dropbox.com").secure / "1" / "datastores" / endpoint

  /**
   * Set authenticate header of OAuth2 for datastore api request header.
   * @param token Access token
   * @return request set authenticate header
   */
  protected def authHeader(token: AccessToken) = Map("Authorization" -> s"Bearer ${token.token}")

  /**
   * Generate endpoint request.
   * @param token Access token
   * @param input decide by implements Requestor
   * @return http request
   */
  private[dropbox4s] def generateReq(token: AccessToken, input: ParamType) = {
    require(Option(token).isDefined && parameterRequirement(input))

    baseUrl <:< authHeader(token) << requestParameter(input)
  }

  /**
   * Defined endpoint parameter requirement.
   *
   * @param input decide by implements Requestor
   * @return satisfy requrement is true, not sataify is false
   */
  protected def parameterRequirement(input: ParamType): Boolean

  /**
   * Mapping input parameter to endpoint.
   *
   * @param input decide by implements Requestor
   * @return parameter mapping
   */
  protected def requestParameter(input: ParamType): Map[String, String]

  /**
   * Request to datastore api endpoint.
   *
   * @param token Access token
   * @param input decide by implements Requestor
   * @return decide by implements Requestor
   */
  def request(token: AccessToken, input: ParamType)(implicit m: Manifest[ResType]): ResType = {
    val request = Http(generateReq(token, input) OK as.String)
    val response = parse(request())

    verifyResponse(response)

    parseJsonToclass(response)
  }

  /**
   * Verify datastore api response.
   * Not found response or conflict response are default checked by this method.
   *
   * @param response
   */
  protected def verifyResponse(response: JValue) {
    verifyErrorResponse("notfound", response)
  }

  /**
   * Veriry error response type.
   *
   * @param errortype key name of error response
   * @param response api response
   */
  protected def verifyErrorResponse(errortype: String, response: JValue) {
    response findField {
      case JField(key, _) if key == errortype => true
      case _ => false
    } match {
      case Some(JField(_, JString(message))) => throw DropboxException(message)
      case None =>
    }
  }

  private def parseJsonToclass(response: JValue)(implicit m: Manifest[ResType]) = response.extract[ResType]
}

/**
 * get_or_create_datastore requestor
 */
object GetOrCreateDatastoreRequestor extends DatastoreApiRequestor[String, GetOrCreateDatastoreResult] {
  val endpoint = "get_or_create_datastore"

  protected def parameterRequirement(dsid: String) = Option(dsid).isDefined && !dsid.isEmpty

  protected def requestParameter(dsid: String) = Map("dsid" -> dsid)
}

/**
 * get_datastore requestor
 */
object GetDatastoreRequestor extends DatastoreApiRequestor[String, GetOrCreateDatastoreResult] {
  val endpoint = "get_datastore"

  protected def parameterRequirement(dsid: String) = Option(dsid).isDefined && !dsid.isEmpty

  protected def requestParameter(dsid: String) = Map("dsid" -> dsid)
}

/**
 * delete_datastore requestor
 */
object DeleteDatastoreRequestor extends DatastoreApiRequestor[String, DeleteDatastoreResult] {
  val endpoint = "delete_datastore"

  protected def parameterRequirement(handle: String) = Option(handle).isDefined && !handle.isEmpty

  protected def requestParameter(handle: String) = Map("handle" -> handle)
}

/**
 * list_datastores requestor
 */
object ListDatastoresRequestor extends DatastoreApiRequestor[AnyRef, ListDatastoresResult] {
  val endpoint = "list_datastores"

  protected def parameterRequirement(input: AnyRef) = notRequired

  protected def requestParameter(input: AnyRef) = noParams

  def request(token: AccessToken): ListDatastoresResult = request(token, null)
}

/**
 * get_snapshot requestor
 */
class GetSnapshotRequestor[T: Manifest] extends DatastoreApiRequestor[String, SnapshotResult[T]] {
  val endpoint = "get_snapshot"

  protected def parameterRequirement(handle: String) = Option(handle).isDefined && !handle.isEmpty

  protected def requestParameter(handle: String) = Map("handle" -> handle)
}
