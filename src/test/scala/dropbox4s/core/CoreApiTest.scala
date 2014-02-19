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
    val uploadCyclePath = DropboxPath("/test_uploadcycle")

    "upload -> DbxEntry.File.update -> DbxEntry.File.remove cycle" in {
      val uploadTestFile = "upload_test.txt"

      val uploadedFile = createFile uploadTo (uploadCyclePath / uploadTestFile)
      uploadedFile.path must equalTo((uploadCyclePath / uploadTestFile).path)

      val updatedFile = uploadedFile update rewriteFile
      updatedFile.path must equalTo((uploadCyclePath / uploadTestFile).path)

      updatedFile remove

      (search(uploadCyclePath, uploadTestFile)) must beEmpty
    }

    "upload -> DropboxPath.remove cycle" in {
      val removeTestFile = "remove_test.txt"

      (createFile uploadTo (uploadCyclePath / removeTestFile)).path must equalTo((uploadCyclePath / removeTestFile).path)

      (uploadCyclePath / removeTestFile) remove

      (search(uploadCyclePath, removeTestFile)) must beEmpty
    }

    "upload -> DbxEntry.File.downloadTo -> DbxEntry.File.remove cycle" in {
      val downloadFileName = "dbx_entry_download_test.txt"
      val downloadFile = new java.io.File(downloadRoot + downloadFileName)

      if(downloadFile.exists) downloadFile.delete
      downloadFile.exists must beFalse

      val uploadedFile = createFile uploadTo (uploadCyclePath / downloadFileName)

      uploadedFile.downloadTo(downloadRoot + downloadFileName)
      downloadFile.exists must beTrue

      uploadedFile remove

      (search(uploadCyclePath, downloadFileName)) must beEmpty
    }

    "upload -> DropboxPath.downloadTo -> DropboxPath.remove cycle" in {
      val downloadFileName = "dropbox_path_download_test.txt"
      val downloadFile = new java.io.File(downloadRoot + downloadFileName)

      if(downloadFile.exists) downloadFile.delete
      downloadFile.exists must beFalse

      createFile uploadTo(uploadCyclePath / downloadFileName)

      (uploadCyclePath / downloadFileName).downloadTo(downloadRoot + downloadFileName)
      downloadFile.exists must beTrue

      (uploadCyclePath / downloadFileName).remove

      (search(uploadCyclePath, downloadFileName)) must beEmpty
    }
  }

}
