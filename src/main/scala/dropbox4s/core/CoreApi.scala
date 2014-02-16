package dropbox4s.core

import com.dropbox.core.DbxClient

/**
 * @author mao.instantlife at gmail.com
 */
object CoreApi {

  def accountInfo(implicit client: DbxClient) = client.getAccountInfo
}
