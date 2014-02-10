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
  def get(rowid: String) = rows.find(_.rowid == rowid)

  def rowDiff(rowid: String, other: T) = {
    require(Option(rowid).isDefined && !rowid.isEmpty && Option(other).isDefined)

    get(rowid) match {
      case Some(target) => {
        implicit val arrays = arrayKeys(converter(target.data) merge converter(other))
        val jsonDiff = converter(target.data) diff converter(other)

        toAtomOps(jsonDiff.changed, putAtomOp) :::
          toAtomOps(jsonDiff.added, putAtomOp) :::
          toAtomOps(jsonDiff.deleted, deleteAtomOp)
      }
      case None => throw DropboxException(s"row-id(${rowid}) is not found.")
    }
  }

  private def arrayKeys(merged: JValue): List[String] = for {
    JObject(field) <- merged
    JField(key, mergedValue) <- field
    if mergedValue.isInstanceOf[JArray]
  } yield key

  private val putAtomOp = {value: JValue => JArray(List(JString("P"), value))}
  private val deleteAtomOp = {value: JValue => JArray(List(JString("D")))}

  private def toAtomOps(diffValues: JValue, op: (JValue) => JValue)(implicit arrayKeys: List[String]) = for {
    JObject(diffField) <- diffValues
    JField(key, differentValue) <- diffField
    if !arrayKeys.exists(_ == key)
  } yield JField(key, op(differentValue))

}

case class TableRow[T](rowid: String, data: T)
