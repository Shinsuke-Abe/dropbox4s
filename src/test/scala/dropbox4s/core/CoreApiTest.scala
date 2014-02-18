package dropbox4s.core

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._
import java.util.Locale
import dropbox4s.core.model.DropboxPath

class CoreApiTest extends Specification with CoreApi {
  val locale = Locale.getDefault
  val version = "1.0"
  val applicationName = "TestAppForLibrary"

  import dropbox4s.datastore.TestConstants._

  implicit val token = testUser1

  val createFile = new java.io.File(this.getClass.getResource("/testfiles/forupload.txt").toURI)
  val rewriteFile = new java.io.File(this.getClass.getResource("/testfiles/forupdate.txt").toURI)

  "accountInfo" should {
    "call DbxClient.getAccountInfo" in {
      accountInfo.userId must equalTo(testUser1Id)
    }
  }

  "file lifecycle" should {
    "upload -> update -> delete cycle" in {
      val uploadedFile = createFile uploadTo DropboxPath("/test_uploadcycle/test.txt")
      uploadedFile.path must equalTo("/test_uploadcycle/test.txt")

      val updatedFile = uploadedFile update rewriteFile
      updatedFile.path must equalTo("/test_uploadcycle/test.txt")

      updatedFile remove
    }
  }

}
