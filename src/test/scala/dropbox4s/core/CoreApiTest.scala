package dropbox4s.core

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._
import java.util.Locale
import dropbox4s.core.model.DropboxPath
import com.dropbox.core.DbxEntry

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

    def uploadFilePath(fileName: String) = uploadCyclePath / fileName

    def downloadFilePath(fileName: String) = downloadRoot + fileName

    "upload -> DbxEntry.File.update -> DbxEntry.File.remove cycle" in {
      val uploadTestFile = "upload_test.txt"

      val uploadedFile = createFile uploadTo uploadFilePath(uploadTestFile)
      uploadedFile.path must equalTo(uploadFilePath(uploadTestFile).path)

      val updatedFile = uploadedFile update rewriteFile
      updatedFile.path must equalTo(uploadFilePath(uploadTestFile).path)

      afterTestByFile(updatedFile, uploadTestFile)
    }

    "upload -> DropboxPath.remove cycle" in {
      val removeTestFile = "remove_test.txt"

      (createFile uploadTo uploadFilePath(removeTestFile)).path must
        equalTo(uploadFilePath(removeTestFile).path)

      afterTestByPath(uploadFilePath(removeTestFile), removeTestFile)
    }

    "upload -> DbxEntry.File.downloadTo -> DbxEntry.File.remove cycle" in {
      val downloadFileName = "dbx_entry_download_test.txt"

      val uploadedFile = prepareDownload(downloadFileName, uploadFilePath(downloadFileName))

      uploadedFile.downloadTo(downloadFilePath(downloadFileName))
      (new java.io.File(downloadFilePath(downloadFileName))).exists must beTrue

      afterTestByFile(uploadedFile, downloadFileName)
    }

    "upload -> DropboxPath.downloadTo -> DropboxPath.remove cycle" in {
      val downloadFileName = "dropbox_path_download_test.txt"

      prepareDownload(downloadFileName, uploadFilePath(downloadFileName))

      uploadFilePath(downloadFileName).downloadTo(downloadFilePath(downloadFileName))
      (new java.io.File(downloadFilePath(downloadFileName))).exists must beTrue

      afterTestByPath(uploadFilePath(downloadFileName), downloadFileName)
    }

    def prepareDownload(downloadFileName: String, toPath: DropboxPath) = {
      val downloadFile = new java.io.File(downloadRoot + downloadFileName)

      if(downloadFile.exists) downloadFile.delete
      downloadFile.exists must beFalse

      createFile uploadTo toPath
    }

    def afterTestByPath(path: DropboxPath, fileName: String) = {
      path.remove

      (search(uploadCyclePath, fileName)) must beEmpty
    }

    def afterTestByFile(file: DbxEntry.File, fileName: String) = {
      file.remove

      (search(uploadCyclePath, fileName)) must beEmpty
    }
  }

}
