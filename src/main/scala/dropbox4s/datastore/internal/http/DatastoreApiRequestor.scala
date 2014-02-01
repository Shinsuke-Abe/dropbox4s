package dropbox4s.datastore.internal.http

import dropbox4s.datastore.auth.AccessToken
import dispatch._
import dropbox4s.datastore.internal.jsons.{GetOrCreateResult, ListDatastoresResult}

/**
 * @author mao.instantlife at gmail.com
 */
trait DatastoreApiRequestor[ParamType, ResType] {
  val baseUrl = host("api.dropbox.com").secure / "1" / "datastores"

  protected def authHeader(token: AccessToken) = Map("Authorization" -> s"Bearer ${token.token}")

  private[dropbox4s] def generateReq(token: AccessToken, input: ParamType): Req
}

object GetOrCreateUrl extends DatastoreApiRequestor[String, GetOrCreateResult] {
  private[dropbox4s] def generateReq(token: AccessToken, dsid: String) = {
    require(Option(dsid).isDefined && !dsid.isEmpty && Option(token).isDefined)

    baseUrl / "get_or_create_datastore" << Map("dsid" -> dsid) <:< authHeader(token)
  }
}

object ListDatastoresUrl extends DatastoreApiRequestor[Unit, ListDatastoresResult] {
  private[dropbox4s] def generateReq(token: AccessToken, input: Unit = ()) = {
    require(Option(token).isDefined)

    baseUrl / "list_datastores" <:< authHeader(token)
  }
}
