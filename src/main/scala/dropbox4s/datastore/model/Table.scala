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

import org.json4s._
import dropbox4s.commons.DropboxException

/**
 * @author mao.instantlife at gmail.com
 */
case class Table[T](handle: String, tid: String, rev: Int, converter: T => JValue, rows: List[TableRow[T]]) {
  def rowDiff(rowid: String, other: T) = {
    require(Option(rowid).isDefined && !rowid.isEmpty && Option(other).isDefined)
    if(!rows.exists(_.rowid == rowid)) throw DropboxException(s"row-id(${rowid}) is not found.")

    val jsonDiff = converter(rows.find(_.rowid == rowid).get.data) diff converter(other)

    diffValues(jsonDiff.changed) ::: diffValues(jsonDiff.added) ::: deleteValues(jsonDiff.deleted)
  }

  private def diffValues(diffValues: JValue) = for {
    JObject(diffField) <- diffValues
    JField(key, differentValue) <- diffField
  } yield JField(key, JArray(List(JString("P"), differentValue)))

  private def deleteValues(deleteValues: JValue) = for {
    JObject(deleteField) <- deleteValues
    JField(key, _) <- deleteField
  } yield JField(key, JArray(List(JString("D"))))
}

case class TableRow[T](rowid: String, data: T)
