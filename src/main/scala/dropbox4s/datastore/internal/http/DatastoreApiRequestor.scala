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

import com.dropbox.core.DbxAuthFinish
import dispatch.Defaults._
import dispatch._
import dropbox4s.commons.DropboxException
import dropbox4s.datastore.internal.jsonresponse._
import dropbox4s.datastore.internal.requestparameter.{CreateDatastoreParameter, ListAwaitParameter, PutDeltaParameter}
import org.json4s.JsonDSL._
import org.json4s.{JValue, _}
import org.json4s.native.JsonMethods._

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
   * @param auth authenticate finish class has access token
   * @return request set authenticate header
   */
  protected def authHeader(auth: DbxAuthFinish) = Map("Authorization" -> s"Bearer ${auth.accessToken}")

  /**
   * Generate endpoint request.
   * @param auth authenticate finish class has access token
   * @param input decide by implements Requestor
   * @return http request
   */
  private[dropbox4s] def generateReq(auth: DbxAuthFinish, input: ParamType) = {
    require(Option(auth).isDefined && parameterRequirement(input))

    baseUrl <:< authHeader(auth) << requestParameter(input)
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
   * @param auth authenticate finish class has access token
   * @param input decide by implements Requestor
   * @return decide by implements Requestor
   */
  def request(auth: DbxAuthFinish, input: ParamType)(implicit m: Manifest[ResType]): ResType = {
    val request = Http(generateReq(auth, input) OK as.String)
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
    verifyErrorResponse("conflict", response)
    verifyErrorResponse("error", response)
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
 * create_datastore
 */
object CreateDatastoreRequestor extends DatastoreApiRequestor[CreateDatastoreParameter, GetOrCreateDatastoreResult] {
  val endpoint: String = "create_datastore"

  protected def parameterRequirement(input: CreateDatastoreParameter) =
    Option(input).isDefined &&
      Option(input.key).isDefined && !input.key.isEmpty

  override protected def requestParameter(input: CreateDatastoreParameter) =
    Map("dsid" -> input.dsid, "key" -> input.key)
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

  def request(auth: DbxAuthFinish): ListDatastoresResult = request(auth, null)
}

/**
 * get_snapshot requestor
 */
object GetSnapshotRequestor extends DatastoreApiRequestor[String, SnapshotResult] {
  val endpoint = "get_snapshot"

  protected def parameterRequirement(handle: String) = Option(handle).isDefined && !handle.isEmpty

  protected def requestParameter(handle: String) = Map("handle" -> handle)
}

/**
 * put_delta requestor
 */
object PutDeltaRequestor extends DatastoreApiRequestor[PutDeltaParameter, PutDeltaResult] {
  val endpoint = "put_delta"

  protected def parameterRequirement(input: PutDeltaParameter) =
    Option(input).isDefined &&
      Option(input.handle).isDefined && !input.handle.isEmpty &&
      Option(input.list_of_changes).isDefined && !input.list_of_changes.isEmpty

  protected def requestParameter(input: PutDeltaParameter) = {
    val parameter = Map(
      "handle" -> input.handle,
      "rev" -> input.rev.toString,
      "changes" -> compact(render(input.changeDeltas))
    )

    if(input.nonce.isDefined) parameter + ("nonce" -> input.nonce.get)
    else parameter
  }
}

/**
 * await for list_datastores_result requestor
 */
object AwaitListDatastoresRequestor extends DatastoreApiRequestor[ListAwaitParameter, AwaitResult] {
  val endpoint = "await"

  protected def parameterRequirement(input: ListAwaitParameter) = notRequired

  protected def requestParameter(input: ListAwaitParameter) =
    Map("list_datastores" -> compact(render(input.toJson)))

  override private[dropbox4s] def generateReq(auth: DbxAuthFinish, input: ListAwaitParameter) = {
    require(Option(auth).isDefined && parameterRequirement(input))

    baseUrl <:< authHeader(auth) <<? requestParameter(input)
  }
}
