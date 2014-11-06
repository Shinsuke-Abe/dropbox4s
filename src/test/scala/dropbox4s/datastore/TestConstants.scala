package dropbox4s.datastore

import java.io.InputStream
import java.util.Properties
import com.dropbox.core.DbxAuthFinish
import scala.util.control.Exception._

/**
 * @author mao.instantlife at gmail.com
 */
object TestConstants {
  val istream = allCatch either this.getClass.getResourceAsStream("/test.properties")

  val prop: Option[Properties] = istream match {
    case Left(_) => None
    case Right(stream) => allCatch opt createAndLoadProperties(stream)
  }

  private def createAndLoadProperties(istream: InputStream) = {
    try {
      val ret = new Properties
      ret.load(istream)
      ret
    } finally {
      istream.close
    }
  }

  private def getPropertyOrEnv(key: String) = prop match {
    case Some(p) => p.getProperty(key)
    case None => sys.env(key)
  }

  val testUser1Token = getPropertyOrEnv("usertoken")
  val testUser1Id = getPropertyOrEnv("userid").toLong
  val testUser1Auth = new DbxAuthFinish(testUser1Token, testUser1Id.toString, null)

  val downloadRoot = getPropertyOrEnv("downloadRoot")
}
