package dropbox4s.datastore.internal.jsons

/**
 * Get or Create Datastore API result.
 *
 * @param handle: datastore's internal identifier. A handle is dbase64 string of 0-1000 characters.
 * @param rev: integer of a specific version or snapshot datastore.
 * @param created: flag what created datastore by executing API.
 *
 * @author mao.instantlife at gmail.com
 */
case class GerOrCreateResult(handle: String, rev: Int, created: Boolean)
