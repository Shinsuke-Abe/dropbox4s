package dropbox4s.datastore

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._
import dropbox4s.datastore.auth.AccessToken

class DatastoreTest extends Specification {
  import dropbox4s.datastore.Datastore._

  implicit val token = AccessToken("test_token")

  "get_or_create" should {
    "throw exception with null value" in {
      get_or_create(null) must throwA[IllegalArgumentException]
    }

    "throw exception with length 0 string" in {
      get_or_create("") must throwA[IllegalArgumentException]
    }

    "get GetOrCreateResult" in {
      get_or_create("test_datastore") must not beNull
    }
  }
}
