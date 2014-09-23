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
import dropbox4s.datastore.internal.jsonresponse._
import scala.Some
import dropbox4s.datastore.model._
import dropbox4s.datastore.internal.requestparameter._
import com.dropbox.core.DbxAuthFinish

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
   * @param auth authenticate finish class has access token
   * @return Datastore instance
   */
  def get(dsid: String, createFlag: Boolean = false)(implicit auth: DbxAuthFinish) =
    if (createFlag) Datastore(dsid, Some(GetOrCreateDatastoreRequestor.request(auth, dsid)))
    else Datastore(dsid, Some(GetDatastoreRequestor.request(auth, dsid)))

  val orCreate = true

  def createShareable(key: String)(implicit auth: DbxAuthFinish) = {
    val parameter = CreateDatastoreParameter(key)
    Datastore(parameter.dsid, Some(CreateDatastoreRequestor.request(auth, parameter)))
  }

  /**
   * get list of datastores.
   *
   * @param auth authenticate finish class has access token
   * @return datastore list and token
   */
  def listDatastores(implicit auth: DbxAuthFinish) = ListDatastoresRequestor.request(auth)

  implicit def listDatastoresToList(list: ListDatastoresResult) = list.datastores
  implicit def datastoresToGetOrCreateResult(ds: Datastore) = ds.result.getOrElse(nullGetOrCreateDsResult)

  implicit class RichDataStore(val ds: LikeDatastore) {
    /**
     * delete datastore.
     *
     * @param auth authenticate finish class has access token
     * @return delete result
     */
    def delete(implicit auth: DbxAuthFinish) = DeleteDatastoreRequestor.request(auth, ds.handle)

    /**
     * get snapshot data list.
     *
     * @param auth authenticate finish class has access token
     * @return Snapshot instance
     */
    def snapshot(implicit auth: DbxAuthFinish) = Snapshot(ds.handle, GetSnapshotRequestor.request(auth, ds.handle))
  }

  implicit class RichListDatastores(val listDs: ListDatastoresResult) {
    def await(implicit auth: DbxAuthFinish) = AwaitListDatastoresRequestor.request(auth, ListAwaitParameter(listDs.token))
  }

  implicit class RichTable[T](val table: Table[T]) {
    private def putDeltaRequest(ops: List[DataOperation], auth: DbxAuthFinish) =
      PutDeltaRequestor.request(auth, PutDeltaParameter(table.handle, table.rev, None, ops))

    private def rowUpdateOps(rowid: String, other: T) =
      table.rowDiff(rowid, other).map(DataUpdate(table.tid, rowid, _))

    /**
     * insert table rows to datastore.
     * Note: if row id of parameter is conflict, throw IllegalArgumentException.
     *
     * @param rows (variable parameter) rows array to insert.
     * @param auth authenticate finish class has access token
     * @return put_delta result
     */
    def insert(rows: TableRow[T]*)(implicit auth: DbxAuthFinish) = {
      require(rows.size == rows.toList.map(_.rowid).distinct.size)

      putDeltaRequest(rows.toList.map(row => DataInsert(table.tid, row.rowid, table.converter(row.data))), auth)
    }

    /**
     * delete rows from datastore.
     * Note: if rowid of parameter is conflict, distinct array before execute.
     *
     * @param rowids (variable parameter) row id array to delete.
     * @param auth authenticate finish class has access token
     * @return put_delta result
     */
    def delete(rowids: String*)(implicit auth: DbxAuthFinish) =
      putDeltaRequest(rowids.distinct.toList.map(DataDelete(table.tid, _)), auth)

    /**
     * delete rows from datastore by condition.
     *
     * @param where delete condition
     * @param auth authenticate finish class has access token
     * @return put_delta result
     */
    def delete(where: (TableRow[T]) => Boolean)(implicit auth: DbxAuthFinish) =
      putDeltaRequest(table.select(where).map(row => DataDelete(table.tid, row.rowid)), auth)

    /**
     * delete all rows of table.
     *
     * @param auth authenticate finish class has access token
     * @return put_delta result
     */
    def truncate(implicit auth: DbxAuthFinish) =
      putDeltaRequest(table.rows.map(row => DataDelete(table.tid, row.rowid)), auth)

    /**
     * update row by rowid
     * @param rowid rowid to update
     * @param other update row data
     * @param auth authenticate finish class has access token
     * @return put_delta result
     */
    def update(rowid: String, other: T)(implicit auth: DbxAuthFinish) =
      putDeltaRequest(rowUpdateOps(rowid, other), auth)

    /**
     * update rows by condition.
     *
     * @param set function for update data
     * @param where update condition
     * @param auth authenticate finish class has access token
     * @return put_delta result
     */
    def update(set: (T) => T)(where: (TableRow[T]) => Boolean)(implicit auth: DbxAuthFinish) =
      putDeltaRequest(table.select(where).map(row => rowUpdateOps(row.rowid, set(row.data))).flatten.toList, auth)
  }

  val nullGetOrCreateDsResult = GetOrCreateDatastoreResult(null, 0, false)
}
