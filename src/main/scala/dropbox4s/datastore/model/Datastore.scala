package dropbox4s.datastore.model

import dropbox4s.datastore.internal.jsons.GetOrCreateResult

/**
 * @author mao.instantlife at gmail.com
 */
case class Datastore(dsid: String, result: Option[GetOrCreateResult]) {

}
