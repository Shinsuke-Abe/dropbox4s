package dropbox4s.datastore.internal.http

import dropbox4s.datastore.auth.AccessToken
import dispatch._, Defaults._
import dropbox4s.datastore.internal.jsons.{GetOrCreateResult, ListDatastoresResult}
import org.json4s._
import org.json4s.native.JsonMethods._

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

    parse(request()).extract
  }
}

object GetOrCreateRequestor extends DatastoreApiRequestor[String, GetOrCreateResult] {
  def apply(token: AccessToken, dsid: String): GetOrCreateResult = executeReq(token, dsid)

  private[dropbox4s] def generateReq(token: AccessToken, dsid: String) = {
    require(Option(dsid).isDefined && !dsid.isEmpty && Option(token).isDefined)

    baseUrl / "get_or_create_datastore" << Map("dsid" -> dsid) <:< authHeader(token)
  }
}

object ListDatastoresUrl extends DatastoreApiRequestor[Unit, ListDatastoresResult] {
  def apply(token: AccessToken, input: Unit = ()): ListDatastoresResult = executeReq(token, input)
  
  private[dropbox4s] def generateReq(token: AccessToken, input: Unit = ()) = {
    require(Option(token).isDefined)

    baseUrl / "list_datastores" <:< authHeader(token)
  }
}
