package dropbox4s.datastore.internal.requestparameter

/**
 * @author mao.instantlife at gmail.com
 */
case class DataInsert[T](tid: String, recordid: String, datadict: T)
