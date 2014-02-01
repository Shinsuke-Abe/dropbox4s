package dropbox4s.datastore.models

/**
 * @author mao.instantlife at gmail.com
 */
case class DsInfo(dsid: String, handle: String, rev: Int, info: Option[InfoDict] = None)
