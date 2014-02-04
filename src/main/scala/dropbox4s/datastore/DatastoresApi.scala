package dropbox4s.datastore

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
import dropbox4s.datastore.internal.jsons.{DsInfo, GetOrCreateDatastoreResult, ListDatastoresResult}
import dropbox4s.datastore.model.Datastore
import dropbox4s.datastore.internal.http.{DeleteDatastoreRequestor, GetDatastoreRequestor, ListDatastoresRequestor, GetOrCreateDatastoreRequestor}

/**
 * @author mao.instantlife at gmail.com
 */
object DatastoresApi {

  def get(dsid: String, createFlag: Boolean = false)(implicit token: AccessToken) = {
    require(Option(dsid).isDefined && !dsid.isEmpty)

    if (createFlag) Datastore(dsid, Some(GetOrCreateDatastoreRequestor.request(token, dsid)))
    else Datastore(dsid, Some(GetDatastoreRequestor.request(token, dsid)))
  }

  val orCreate = true

  def listDatastores(implicit token: AccessToken) = ListDatastoresRequestor.request(token)

  implicit def listDatastoresToList(list: ListDatastoresResult) = list.datastores

  implicit def datastoresToGetOrCreateResult(ds: Datastore) = ds.result.getOrElse(nullGetOrCreateDsResult)

  implicit class RichDataStore(val ds: Datastore) {
    def delete(implicit token: AccessToken) = DeleteDatastoreRequestor.request(token, ds.handle)
  }

  implicit class RichDsInfo(val dsInfo: DsInfo) {
    def delete(implicit token: AccessToken) = DeleteDatastoreRequestor.request(token, dsInfo.handle)
  }

  val nullGetOrCreateDsResult = GetOrCreateDatastoreResult(null, 0, false)
}
