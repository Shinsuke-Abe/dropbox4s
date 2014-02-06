package dropbox4s.datastore.model

import dropbox4s.datastore.internal.jsonresponse.SnapshotResult
import org.json4s.JsonAST.JValue

/**
 * @author mao.instantlife at gmail.com
 */
case class Snapshot(handle: String, result: SnapshotResult[JValue]) {
  def tableNames = result.rows.map(_.tid).distinct
}
