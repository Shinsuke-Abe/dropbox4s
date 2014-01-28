package dropbox4s.datastore.auth

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._

class AccessTokenTest extends Specification {
  "constructor" should {
    "throw exception with null" in {
      AccessToken(null) must throwA[IllegalArgumentException]
    }

    "throw exception with empty string" in {
      AccessToken("") must throwA[IllegalArgumentException]
    }
  }
}
