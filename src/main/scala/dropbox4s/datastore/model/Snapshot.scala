package dropbox4s.datastore.model

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

import dropbox4s.datastore.internal.jsonresponse.SnapshotResult
import org.json4s.JsonAST.JValue
import org.json4s.DefaultFormats

/**
 * @author mao.instantlife at gmail.com
 */
case class Snapshot(handle: String, result: SnapshotResult) {
  implicit val format = DefaultFormats

  def tableNames = result.rows.map(_.tid).distinct

  def table[T: Manifest](name: String)(generator: T => JValue) = Table(
    handle,
    result.role,
    name,
    result.rev,
    generator,
    result.rows.filter(_.tid == name).map(row => TableRow(row.rowid, row.data.extract[T])))
}
