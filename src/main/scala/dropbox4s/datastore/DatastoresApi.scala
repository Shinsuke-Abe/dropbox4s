package dropbox4s.datastore

import dropbox4s.datastore.auth.AccessToken
import dropbox4s.datastore.internal.jsons.{GetOrCreateResult, ListDatastoresResult}
import dropbox4s.datastore.model.Datastore
import dropbox4s.datastore.internal.http.{ListDatastoresRequestor, GetOrCreateRequestor}

/**
 * @author mao.instantlife at gmail.com
 */
object DatastoresApi {

  def get_or_create(dsid: String)(implicit token: AccessToken) = {
    require(Option(dsid).isDefined && !dsid.isEmpty)

    Datastore(dsid, Some(GetOrCreateRequestor(token, dsid)))
  }

  def list_datastores(implicit token: AccessToken) = ListDatastoresRequestor(token)

  implicit def listDatastoresToList(list: ListDatastoresResult) = list.datastores

  implicit def datastoresToGetOrCreateResult(ds: Datastore) = ds.result.getOrElse(nullGetOrCreateDsResult)

  val nullGetOrCreateDsResult = GetOrCreateResult(null, 0, false)
}
