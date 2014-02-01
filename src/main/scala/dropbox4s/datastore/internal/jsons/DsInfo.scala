package dropbox4s.datastore.internal.jsons

/**
 * @author mao.instantlife at gmail.com
 */
case class DsInfo(dsid: String, handle: String, rev: Int, info: Option[InfoDict] = None)
