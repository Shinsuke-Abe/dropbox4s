package dropbox4s.datastore

import dropbox4s.datastore.models.GerOrCreateResult

/**
 * @author mao.instantlife at gmail.com
 */
object Datastore {

  def get_or_create(dsid: String): GerOrCreateResult = {
    require(Option(dsid).isDefined && !dsid.isEmpty)
    GerOrCreateResult("test_handle", 0, true)
  }
}
