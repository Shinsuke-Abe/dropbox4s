package dropbox4s.datastore

import java.util.Properties
import dropbox4s.commons.auth.AccessToken

/**
 * @author mao.instantlife at gmail.com
 */
object TestConstants {
  val istream = this.getClass.getResourceAsStream("/test.properties")

  val prop = new Properties()
  prop.load(istream)

  istream.close

  val testUser1 = AccessToken(prop.getProperty("usertoken"))
  val testUser1Id = prop.getProperty("userid").toLong
}
