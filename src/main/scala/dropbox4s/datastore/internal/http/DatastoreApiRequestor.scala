package dropbox4s.datastore.internal.http

import dropbox4s.datastore.auth.AccessToken

/**
 * @author mao.instantlife at gmail.com
 */
object DatastoreApiRequestor {
  def getOrCreateUrl(dsid: String, token: AccessToken) = {
    require(Option(dsid).isDefined && !dsid.isEmpty && Option(token).isDefined)
  }
}
