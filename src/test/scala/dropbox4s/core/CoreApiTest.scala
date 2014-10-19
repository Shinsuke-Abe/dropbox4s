package dropbox4s.core

/**
 * @author mao.instantlife at gmail.com
 */

import dropbox4s.commons.DropboxException
import org.specs2.mutable._
import java.util.Locale
import dropbox4s.core.model.DropboxPath
import com.dropbox.core.{DbxThumbnailSize, DbxEntry}

class CoreApiTest extends Specification with CoreApi {
  val version = "1.0"
  val applicationName = "TestAppForLibrary"

  import dropbox4s.datastore.TestConstants._

  implicit val auth = testUser1Auth

  val createFile = new java.io.File(this.getClass.getResource("/testfiles/forupload.txt").toURI)
  val createImageFile = new java.io.File(this.getClass.getResource("/testfiles/foruploadimage.jpg").toURI)
  val rewriteFile = new java.io.File(this.getClass.getResource("/testfiles/forupdate.txt").toURI)

  "clientIdentifier" should {
    "has dropbox4s version" in {
      clientIdentifier must equalTo("TestAppForLibrary/1.0 dropbox4s/0.2.0")
    }
  }

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

    "upload image -> DbxEntry.File.thumbnail -> DbxEntry.File.remove cycle" in {
      val forThumbnailImage = "thumbnail_test.jpg"
      val thumbnailImage = "thumbnail.png"

      val uploadedFile = prepareCycleTest(thumbnailImage, uploadFilePath(forThumbnailImage), createImageFile)

      // download thumbnail
      val thumbnailFile = uploadedFile.thumbnail(DbxThumbnailSize.w64h64, downloadFilePath(thumbnailImage))
      thumbnailFile must beAnInstanceOf[DbxEntry.File]
      thumbnailFile.isFile must beTrue

      val downloadedThumbnail = new java.io.File(downloadFilePath(thumbnailImage))
      downloadedThumbnail.exists must beTrue
      downloadedThumbnail.getName must endWith(".png")

      afterTestByFile(uploadedFile, forThumbnailImage)
    }

    "upload text -> DbxEntry.File.thumbnail -> throw DropboxException -> DbxEntry.File.remove cycle" in {
      val forThumbnailFile = "thumbnail_test.txt"
      val thumbnailImage = "thumbnail.png"

      val uploadedFile = prepareCycleTest(forThumbnailFile, uploadFilePath(forThumbnailFile))

      // download thumbnail
      uploadedFile.thumbnail(DbxThumbnailSize.w64h64, downloadFilePath(thumbnailImage)) must
        throwA[DropboxException](message = s"file have not thumbnail. file = ")
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

    def prepareCycleTest(downloadFileName: String, toPath: DropboxPath, uploadFile: java.io.File = createFile) = {
      val downloadFile = new java.io.File(downloadRoot + downloadFileName)

      if(downloadFile.exists) downloadFile.delete
      downloadFile.exists must beFalse

      uploadFile uploadTo toPath
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

    "upload -> DropboxPath.copyRef to DropboxPath.copyFrom -> search(to) -> remove(from) -> remove(to)" in {
      val copyFilename = "dropbox_path_copy_ref_test.txt"
      val copyDestPath = DropboxPath("/test_copycycle") / "from_res_file.txt"

      val uploadedFile = prepareCycleTest(copyFilename, uploadFilePath(copyFilename))

      val newRef = uploadedFile.copyRef

      copyDestPath copyFrom newRef

      (search(DropboxPath("/test_copycycle"), "from_res_file.txt")) must have size(1)

      afterTestByPath(uploadCyclePath / copyFilename, copyFilename)
      afterTestByPath(copyDestPath, "from_res_file.txt")
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

    "createFolder -> children has 1 -> remove children -> children has none" in {
      val createBaseFolder = DropboxPath("/test_create_folder")
      val targetFolder = createBaseFolder / "created"

      createFolder(targetFolder)

      (createBaseFolder children).size must equalTo(1)

      targetFolder remove

      (createBaseFolder children).size must equalTo(0)
    }
  }

}
