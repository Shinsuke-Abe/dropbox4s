package dropbox4s.datastore

import java.util.Properties
import com.dropbox.core.DbxAuthFinish

/**
 * @author mao.instantlife at gmail.com
 */
object TestConstants {
  val istream = this.getClass.getResourceAsStream("/test.properties")

  val prop = new Properties()
  prop.load(istream)

  istream.close

  val testUser1Token = prop.getProperty("usertoken")
  val testUser1Id = prop.getProperty("userid").toLong
  val testUser1Auth = new DbxAuthFinish(testUser1Token, testUser1Id.toString, null)

  val downloadRoot = prop.getProperty("downloadRoot")
}
