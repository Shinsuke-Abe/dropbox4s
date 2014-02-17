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

import dropbox4s.datastore.internal.http._
import dropbox4s.commons.auth.AccessToken
import dropbox4s.datastore.internal.jsonresponse._
import scala.Some
import dropbox4s.datastore.model._
import dropbox4s.datastore.internal.requestparameter._

/**
 * @author mao.instantlife at gmail.com
 */
object DatastoresApi {
  /**
   * get datastore by dsid.
   * if set createFlag to true, call get_or_create endpoint.
   *
   * @param dsid datastore id
   * @param createFlag create datastore if dsid not exists.
   * @param token access token
   * @return Datastore instance
   */
  def get(dsid: String, createFlag: Boolean = false)(implicit token: AccessToken) =
    if (createFlag) Datastore(dsid, Some(GetOrCreateDatastoreRequestor.request(token, dsid)))
    else Datastore(dsid, Some(GetDatastoreRequestor.request(token, dsid)))

  val orCreate = true

  /**
   * get list of datastores.
   *
   * @param token access token
   * @return datastore list and token
   */
  def listDatastores(implicit token: AccessToken) = ListDatastoresRequestor.request(token)

  implicit def listDatastoresToList(list: ListDatastoresResult) = list.datastores
  implicit def datastoresToGetOrCreateResult(ds: Datastore) = ds.result.getOrElse(nullGetOrCreateDsResult)

  implicit class RichDataStore(val ds: LikeDatastore) {
    /**
     * delete datastore.
     *
     * @param token access token
     * @return delete result
     */
    def delete(implicit token: AccessToken) = DeleteDatastoreRequestor.request(token, ds.handle)

    /**
     * get snapshot data list.
     *
     * @param token access token
     * @return Snapshot instance
     */
    def snapshot(implicit token: AccessToken) = Snapshot(ds.handle, GetSnapshotRequestor.request(token, ds.handle))
  }

  implicit class RichListDatastores(val listDs: ListDatastoresResult) {
    def await(implicit token: AccessToken) = AwaitListDatastoresRequestor.request(token, ListAwaitParameter(listDs.token))
  }

  implicit class RichTable[T](val table: Table[T]) {
    private def putDeltaRequest(ops: List[DataOperation], token: AccessToken) =
      PutDeltaRequestor.request(token, PutDeltaParameter(table.handle, table.rev, None, ops))

    private def select(where: (TableRow[T]) => Boolean) = table.rows.filter(where)

    private def rowUpdateOps(rowid: String, other: T) =
      table.rowDiff(rowid, other).map(DataUpdate(table.tid, rowid, _))

    /**
     * insert table rows to datastore.
     * Note: if row id of parameter is conflict, throw IllegalArgumentException.
     *
     * @param rows (variable parameter) rows array to insert.
     * @param token access token
     * @return put_delta result
     */
    def insert(rows: TableRow[T]*)(implicit token: AccessToken) = {
      require(rows.size == rows.toList.map(_.rowid).distinct.size)

      putDeltaRequest(rows.toList.map(row => DataInsert(table.tid, row.rowid, table.converter(row.data))), token)
    }

    /**
     * delete rows from datastore.
     * Note: if rowid of parameter is conflict, distinct array before execute.
     *
     * @param rowids (variable parameter) row id array to delete.
     * @param token access token
     * @return put_delta result
     */
    def delete(rowids: String*)(implicit token: AccessToken) =
      putDeltaRequest(rowids.distinct.toList.map(DataDelete(table.tid, _)), token)

    /**
     * delete rows from datastore by condition.
     *
     * @param where delete condition
     * @param token access token
     * @return put_delta result
     */
    def delete(where: (TableRow[T]) => Boolean)(implicit token: AccessToken) =
      putDeltaRequest(select(where).map(row => DataDelete(table.tid, row.rowid)), token)

    /**
     * delete all rows of table.
     *
     * @param token access token
     * @return put_delta result
     */
    def truncate(implicit token: AccessToken) =
      putDeltaRequest(table.rows.map(row => DataDelete(table.tid, row.rowid)), token)

    /**
     * update row by rowid
     * @param rowid rowid to update
     * @param other update row data
     * @param token access token
     * @return put_delta result
     */
    def update(rowid: String, other: T)(implicit token: AccessToken) =
      putDeltaRequest(rowUpdateOps(rowid, other), token)

    /**
     * update rows by condition.
     *
     * @param set function for update data
     * @param where update condition
     * @param token access token
     * @return put_delta result
     */
    def update(set: (T) => T)(where: (TableRow[T]) => Boolean)(implicit token: AccessToken) =
      putDeltaRequest(select(where).map(row => rowUpdateOps(row.rowid, set(row.data))).flatten.toList, token)
  }

  val nullGetOrCreateDsResult = GetOrCreateDatastoreResult(null, 0, false)
}
