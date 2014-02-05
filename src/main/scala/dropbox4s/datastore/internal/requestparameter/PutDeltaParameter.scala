package dropbox4s.datastore.internal.requestparameter

/**
 * @author mao.instantlife at gmail.com
 */
case class PutDeltaParameter[T](handle: String, rev: Int, nonce: Option[String], list_of_changes: List[DataInsert[T]])
