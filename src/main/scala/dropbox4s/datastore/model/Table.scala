package dropbox4s.datastore.model

import org.json4s.JsonAST.JValue

/**
 * @author mao.instantlife at gmail.com
 */
case class Table[T](handle: String, tid: String, rev: Int, generator: T => JValue, rows: List[TableRow[T]])

case class TableRow[T](rowid: String, data: T)
