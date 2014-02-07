package dropbox4s.datastore.internal.requestparameter

import org.json4s._

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

/**
 * @author mao.instantlife at gmail.com
 */
case class PutDeltaParameter(handle: String, rev: Int, nonce: Option[String], list_of_changes: List[DataOperation]) {
  def changeDeltas = list_of_changes.map(_.toChangeList)
}

sealed trait DataOperation {
  val op: String
  def toChangeList: JValue
}

case class DataInsert(tid: String, recordid: String, datadict: JValue) extends DataOperation {
  val op = "I"

  def toChangeList = JArray(List(JString(op), JString(tid), JString(recordid), datadict))
}

case class DataDelete(tid: String, recordid: String) extends DataOperation {
  val op = "D"

  def toChangeList = JArray(List(JString(op), JString(tid), JString(recordid)))
}
