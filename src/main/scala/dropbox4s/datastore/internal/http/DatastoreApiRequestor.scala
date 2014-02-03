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
import dropbox4s.datastore.internal.jsons.{DeleteDatastoreResult, GetOrCreateDatastoreResult, ListDatastoresResult}
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

  lazy val baseUrl = host("api.dropbox.com").secure / "1" / "datastores" / endpoint

  /**
   * Set authenticate header of OAuth2 for datastore api request header.
   * @param token Access token
   * @return request set authenticate header
   */
  protected def authHeader(token: AccessToken) = Map("Authorization" -> s"Bearer ${token.token}")

  private[dropbox4s] def generateReq(token: AccessToken, input: ParamType): Req

  /**
   * Execute datastore api request.
   *
   * @param token Access token
   * @param input decide in implements Requestor
   * @return decide in implements Requestor
   */
  protected def executeReq(token: AccessToken, input: ParamType): ResType = {
    val request = Http(generateReq(token, input) OK as.String)
    val response = parse(request())

    verifyResponse(response)

    parseJsonToclass(response)
  }

  protected def verifyResponse(response: JValue) {
    verifyErrorResponse("notfound", response)
  }

  protected def verifyErrorResponse(errortype: String, response: JValue) {
    val notfound = response findField {
      case JField(key, _) if key == errortype => true
      case _ => false
    }

    if(notfound.isDefined)
      notfound.get match {
        case JField(key, JString(message)) if key == errortype => throw DropboxException(message)
      }
  }

  protected def parseJsonToclass(response: JValue): ResType
}

/**
 * get_or_create_datastore requestor
 */
object GetOrCreateDatastoreRequestor extends DatastoreApiRequestor[String, GetOrCreateDatastoreResult] {
  val endpoint = "get_or_create_datastore"

  def apply(token: AccessToken, dsid: String): GetOrCreateDatastoreResult = executeReq(token, dsid)

  private[dropbox4s] def generateReq(token: AccessToken, dsid: String) = {
    require(Option(dsid).isDefined && !dsid.isEmpty && Option(token).isDefined)

    baseUrl << Map("dsid" -> dsid) <:< authHeader(token)
  }

  protected def parseJsonToclass(response: JValue) = response.extract[GetOrCreateDatastoreResult]
}

/**
 * get_datastore requestor
 */
object GetDatastoreRequestor extends DatastoreApiRequestor[String, GetOrCreateDatastoreResult] {
  val endpoint = "get_datastore"

  def apply(token: AccessToken, dsid: String): GetOrCreateDatastoreResult = executeReq(token, dsid)

  private[dropbox4s] def generateReq(token: AccessToken, dsid: String) = {
    require(Option(dsid).isDefined && !dsid.isEmpty && Option(token).isDefined)

    baseUrl << Map("dsid" -> dsid) <:< authHeader(token)
  }

  protected def parseJsonToclass(response: JValue) = response.extract[GetOrCreateDatastoreResult]
}

/**
 * delete_datastore requestor
 */
object DeleteDatastoreRequestor extends DatastoreApiRequestor[String, DeleteDatastoreResult] {
  val endpoint = "delete_datastore"

  def apply(token: AccessToken, handle: String):DeleteDatastoreResult = executeReq(token, handle)

  private[dropbox4s] def generateReq(token: AccessToken, handle: String) = {
    require(Option(handle).isDefined && !handle.isEmpty && Option(token).isDefined)

    baseUrl << Map("handle" -> handle) <:< authHeader(token)
  }

  protected def parseJsonToclass(response: JValue) = response.extract[DeleteDatastoreResult]
}

/**
 * list_datastores requestor
 */
object ListDatastoresRequestor extends DatastoreApiRequestor[Unit, ListDatastoresResult] {
  val endpoint = "list_datastores"

  def apply(token: AccessToken, input: Unit = ()): ListDatastoresResult = executeReq(token, input)
  
  private[dropbox4s] def generateReq(token: AccessToken, input: Unit = ()) = {
    require(Option(token).isDefined)

    baseUrl <:< authHeader(token)
  }

  protected def parseJsonToclass(response: JValue) = response.extract[ListDatastoresResult]
}
