package dropbox4s.datastore.internal.jsons

/**
 * @author mao.instantlife at gmail.com
 */
case class Row[T](tid: String, rowid: String, data: T)
