package dropbox4s.datastore.internal.http

import dropbox4s.datastore.auth.AccessToken
import dispatch._

/**
 * @author mao.instantlife at gmail.com
 */
object DatastoreApiRequestor {
  def getOrCreateUrl(dsid: String, token: AccessToken) = {
    require(Option(dsid).isDefined && !dsid.isEmpty && Option(token).isDefined)
    host("api.dropbox.com").secure / "1" / "datastores" / "get_or_create_datastore" <<
      Map("dsid" -> dsid) <:<
      Map("Authorization" -> s"Bearer ${token.token}")
  }
}
