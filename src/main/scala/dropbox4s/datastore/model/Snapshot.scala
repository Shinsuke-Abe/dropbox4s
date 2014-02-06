package dropbox4s.datastore.model

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
    name,
    result.rev,
    generator,
    result.rows.filter(_.tid == name).map(row => TableRow(row.rowid, row.data.extract[T])))
}
