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

      val uploadedFile = prepareCycleTest(downloadFileName, uploadFilePath(downloadFileName))

      uploadedFile.downloadTo(downloadFilePath(downloadFileName))
      (new java.io.File(downloadFilePath(downloadFileName))).exists must beTrue

      afterTestByFile(uploadedFile, downloadFileName)
    }

    "upload -> DropboxPath.downloadTo -> DropboxPath.remove cycle" in {
      val downloadFileName = "dropbox_path_download_test.txt"

      prepareCycleTest(downloadFileName, uploadFilePath(downloadFileName))

      uploadFilePath(downloadFileName).downloadTo(downloadFilePath(downloadFileName))
      (new java.io.File(downloadFilePath(downloadFileName))).exists must beTrue

      afterTestByPath(uploadFilePath(downloadFileName), downloadFileName)
    }

    def prepareCycleTest(downloadFileName: String, toPath: DropboxPath) = {
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

    "upload -> DbxEntry.File.copyTo -> search(to) -> remove(from) -> remove(to)" in {
      val copyFilename = "dbx_entry_copy_test.txt"
      val copyDestPath = DropboxPath("/test_copycycle")

      val uploadedFile = prepareCycleTest(copyFilename, uploadFilePath(copyFilename))

      uploadedFile copyTo (copyDestPath / copyFilename)

      verifyCopyFile(copyDestPath, copyFilename)
    }

    "upload -> DropboxPath.copyTo -> search(to) -> remove(from) -> remove(to)" in {
      val copyFilename = "dropbox_path_copy_test.txt"
      val copyDestPath = DropboxPath("/test_copycycle")

      prepareCycleTest(copyFilename, uploadFilePath(copyFilename))

      uploadFilePath(copyFilename) copyTo (copyDestPath / copyFilename)

      verifyCopyFile(copyDestPath, copyFilename)
    }

    def verifyCopyFile(dest: DropboxPath, fileName: String) = {
      (search(dest, fileName)) must have size(1)

      afterTestByPath(uploadCyclePath / fileName, fileName)
      afterTestByPath(dest / fileName, fileName)
    }

    "upload -> DbxEntry.File.moveTo -> search(from) -> search(to) -> remove(to)" in {
      val moveFilename = "dbx_entry_move_test.txt"
      val moveDestPath = DropboxPath("/test_movecycle")

      val uploadedFile = prepareCycleTest(moveFilename, uploadFilePath(moveFilename))

      uploadedFile moveTo (moveDestPath / moveFilename)

      verifyMoveFile(moveDestPath, moveFilename)
    }

    "upload -> DropboxPath.moveTo -> search(from) -> search(to) -> remove(to)" in {
      val moveFilename = "dropbox_path_move_test.txt"
      val moveDestPath = DropboxPath("/test_movecycle")

      prepareCycleTest(moveFilename, uploadFilePath(moveFilename))

      uploadFilePath(moveFilename) moveTo (moveDestPath / moveFilename)

      verifyMoveFile(moveDestPath, moveFilename)
    }

    def verifyMoveFile(dest: DropboxPath, fileName: String) = {
      (search(uploadCyclePath, fileName)) must beEmpty
      (search(dest, fileName)) must have size(1)

      afterTestByPath(dest / fileName, fileName)
    }
  }

}
