package dropbox4s.datastore.internal.jsons

/**
 * @author mao.instantlife at gmail.com
 */
case class SnapshotResult[T](rows: List[Row[T]], rev: Int)
