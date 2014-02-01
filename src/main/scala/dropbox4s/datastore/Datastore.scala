package dropbox4s.datastore

import dropbox4s.datastore.auth.AccessToken
import dropbox4s.datastore.internal.jsons.GetOrCreateResult

/**
 * @author mao.instantlife at gmail.com
 */
object Datastore {

  def get_or_create(dsid: String)(implicit token: AccessToken): GetOrCreateResult = {
    require(Option(dsid).isDefined && !dsid.isEmpty)
    GetOrCreateResult("test_handle", 0, true)
  }
}
