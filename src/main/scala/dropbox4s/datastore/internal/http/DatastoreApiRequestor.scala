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
import dropbox4s.datastore.internal.jsons.{DeleteDatastoreResult, GetOrCreateResult, ListDatastoresResult}
import org.json4s._
import org.json4s.native.JsonMethods._
import dropbox4s.commons.DropboxException
import org.json4s.JValue

/**
 * @author mao.instantlife at gmail.com
 */
trait DatastoreApiRequestor[ParamType, ResType] {
  implicit val format = DefaultFormats

  val baseUrl = host("api.dropbox.com").secure / "1" / "datastores"

  protected def authHeader(token: AccessToken) = Map("Authorization" -> s"Bearer ${token.token}")

  private[dropbox4s] def generateReq(token: AccessToken, input: ParamType): Req

  protected def executeReq(token: AccessToken, input: ParamType): ResType = {
    val request = Http(generateReq(token, input) OK as.String)
    val response = parse(request())

    verifyResponse(response)

    parseJsonToclass(response)
  }

  protected def verifyResponse(response: JValue) {}

  protected def parseJsonToclass(response: JValue): ResType
}

/**
 * get_or_create_datastore requestor
 */
object GetOrCreateRequestor extends DatastoreApiRequestor[String, GetOrCreateResult] {
  def apply(token: AccessToken, dsid: String): GetOrCreateResult = executeReq(token, dsid)

  private[dropbox4s] def generateReq(token: AccessToken, dsid: String) = {
    require(Option(dsid).isDefined && !dsid.isEmpty && Option(token).isDefined)

    baseUrl / "get_or_create_datastore" << Map("dsid" -> dsid) <:< authHeader(token)
  }

  protected def parseJsonToclass(response: JValue) = response.extract[GetOrCreateResult]
}

/**
 * get_datastore requestor
 */
object GetRequestor extends DatastoreApiRequestor[String, GetOrCreateResult] {
  def apply(token: AccessToken, dsid: String): GetOrCreateResult = executeReq(token, dsid)

  private[dropbox4s] def generateReq(token: AccessToken, dsid: String) = {
    require(Option(dsid).isDefined && !dsid.isEmpty && Option(token).isDefined)

    baseUrl / "get_datastore" << Map("dsid" -> dsid) <:< authHeader(token)
  }

  override protected def verifyResponse(response: JValue) {
    val notfound = response findField {
      case JField("notfound", _) => true
      case _ => false
    }

    if(notfound.isDefined)
      notfound.get match { case JField("notfound", JString(message)) => throw DropboxException(message) }
  }

  protected def parseJsonToclass(response: JValue) = response.extract[GetOrCreateResult]
}

/**
 * delete_datastore requestor
 */
object DeleteDatastoreRequestor extends DatastoreApiRequestor[String, DeleteDatastoreResult] {
  def apply(token: AccessToken, handle: String):DeleteDatastoreResult = executeReq(token, handle)

  private[dropbox4s] def generateReq(token: AccessToken, handle: String) = {
    require(Option(handle).isDefined && !handle.isEmpty && Option(token).isDefined)

    baseUrl / "delete_datastore" << Map("handle" -> handle) <:< authHeader(token)
  }

  override protected def verifyResponse(response: JValue) {
    val notfound = response findField {
      case JField("notfound", _) => true
      case _ => false
    }

    if(notfound.isDefined)
      notfound.get match { case JField("notfound", JString(message)) => throw DropboxException(message) }
  }

  protected def parseJsonToclass(response: JValue) = response.extract[DeleteDatastoreResult]
}

/**
 * list_datastores requestor
 */
object ListDatastoresRequestor extends DatastoreApiRequestor[Unit, ListDatastoresResult] {
  def apply(token: AccessToken, input: Unit = ()): ListDatastoresResult = executeReq(token, input)
  
  private[dropbox4s] def generateReq(token: AccessToken, input: Unit = ()) = {
    require(Option(token).isDefined)

    baseUrl / "list_datastores" <:< authHeader(token)
  }

  protected def parseJsonToclass(response: JValue) = response.extract[ListDatastoresResult]
}
