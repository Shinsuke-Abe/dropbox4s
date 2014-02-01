package dropbox4s.datastore.internal.http

import dropbox4s.datastore.auth.AccessToken
import dispatch._

/**
 * @author mao.instantlife at gmail.com
 */
object DatastoreApiRequestor {
  val baseUrl = host("api.dropbox.com").secure / "1" / "datastores"

  def getOrCreateUrl(dsid: String, token: AccessToken) = {
    require(Option(dsid).isDefined && !dsid.isEmpty && Option(token).isDefined)

    baseUrl / "get_or_create_datastore" << Map("dsid" -> dsid) <:< authHeader(token)
  }

  def listDatastoresUrl(token: AccessToken) = {
    require(Option(token).isDefined)

    baseUrl / "list_datastores" <:< authHeader(token)
  }

  private def authHeader(token: AccessToken) = Map("Authorization" -> s"Bearer ${token.token}")
}
