package dropbox4s.core

/*
 * Copyright (C) 2014 Shinsuke Abe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.dropbox.core._
import java.util.Locale
import java.io.{File, FileOutputStream, FileInputStream}
import collection.JavaConversions._
import dropbox4s.core.model.DropboxPath

/**
 * @author mao.instantlife at gmail.com
 */
trait CoreApi {
  val applicationName: String
  val version: String
  val locale: Locale = Locale.getDefault

  lazy val clientIdentifier = s"${applicationName}/${version} dropbox4s/0.1.0"
  lazy val requestConfig = new DbxRequestConfig(clientIdentifier, locale.toString)

  lazy val client = new DbxClient(requestConfig, _: String)

  /**
   * get the user's account information.<br/>
   * more detail, see the <a href="http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/com/dropbox/core/DbxClient.html#getAccountInfo%28%29">SDK javadoc</a>
   *
   * @param auth authenticate finish class has access token
   * @return result of DbxClient.getAccountInfo
   */
  def accountInfo(implicit auth: DbxAuthFinish) = client(auth.accessToken).getAccountInfo

  /**
   * search file or folder on Dropbox by path and query of name.<br/>
   * more detail, see the <a href="http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/com/dropbox/core/DbxClient.html#searchFileAndFolderNames%28java.lang.String,%20java.lang.String%29">SDK javadoc</a>
   *
   * @param path path to search under
   * @param query A space-separated list of substrings to search for. A file matches only if it contains all the substrings.
   * @param auth authenticate finish class has access token
   * @return result of DbxClient.searchFileAndFolderNames
   */
  def search(path: DropboxPath, query: String)(implicit auth: DbxAuthFinish): List[DbxEntry] =
    client(auth.accessToken).searchFileAndFolderNames(path.path, query).toList

  /**
   * create new folder in Dropbox.<br/>
   * more detail, see the <a href="http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/com/dropbox/core/DbxClient.html#createFolder%28java.lang.String%29">SDK javadoc</a>
   *
   * @param path path to create folder
   * @param auth authenticate finish class has access token
   * @return result of DbxClient.createFolder
   */
  def createFolder(path: DropboxPath)(implicit auth: DbxAuthFinish) =
    client(auth.accessToken).createFolder(path.path)

  implicit class DbxRichFile(val localFile: File) {
    /**
     * upload file to Dropbox.<br/>
     * more detail, see the <a href="http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/com/dropbox/core/DbxClient.html#uploadFile%28java.lang.String,%20com.dropbox.core.DbxWriteMode,%20long,%20java.io.InputStream%29">SDK javadoc</a>
     *
     * @param to path to upload folder.
     * @param isForce if set to true, force upload. if set to false(default), file renamed automatically.
     * @param auth authenticate finish class has access token
     * @return result of DbxClient.uploadFile
     */
    def uploadTo(to: DropboxPath, isForce: Boolean = false)(implicit auth: DbxAuthFinish) = asUploadFile(localFile){ (file, stream) =>
      if(isForce) client(auth.accessToken).uploadFile(to.path, DbxWriteMode.force, localFile.length, stream)
      else client(auth.accessToken).uploadFile(to.path, DbxWriteMode.add, localFile.length, stream)
    }
  }

  implicit class DbxRichEntryFile(val fileEntity: DbxEntry.File) {
    /**
     * update by new file.<br/>
     * more detail, see the <a href="http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/com/dropbox/core/DbxClient.html#uploadFile%28java.lang.String,%20com.dropbox.core.DbxWriteMode,%20long,%20java.io.InputStream%29">SDK javadoc</a>
     *
     * @param newFile new file instance for update.
     * @param auth authenticate finish class has access token
     * @return result of DbxClient.uploadFile
     */
    def update(newFile: File)(implicit auth: DbxAuthFinish) = asUploadFile(newFile){ (file, stream) =>
      client(auth.accessToken).uploadFile(fileEntity.path, DbxWriteMode.update(fileEntity.rev), newFile.length, stream)
    }
  }

  implicit class RichDropboxPath(val dropboxPath: DropboxPath) {
    /**
     * get metadata of children for a receiver's path.<br/>
     * more detail, see the <a href="http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/com/dropbox/core/DbxClient.html#getMetadataWithChildren%28java.lang.String%29">SDK javadoc</a>
     *
     * @param auth authenticate finish class has access token
     * @return result of DbxClient.getMetadataWithChildren
     */
    def children(implicit auth: DbxAuthFinish) = client(auth.accessToken).getMetadataWithChildren(dropboxPath.path)

    /**
     * get revisions for a receiver's path.<br/>
     * more detail, see the <a href="http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/com/dropbox/core/DbxClient.html#getRevisions%28java.lang.String%29">SDK javadoc</a>
     *
     * @param auth authenticate finish class has access token
     * @return result of DbxClient.getRevisions
     */
    def revisions(implicit auth: DbxAuthFinish) = client(auth.accessToken).getRevisions(dropboxPath.path)
  }

  implicit def FileEntryToRichPath(fileEntity: DbxEntry.File) = RichPath(fileEntity.path, fileEntity.rev)
  implicit def DropboxPathToRichPath(dropboxPath: DropboxPath) = RichPath(dropboxPath.path)

  case class RichPath(val path: String, val rev: String = null) {
    /**
     * download file on Dropbox file to local path.<br/>
     * more detail, see the <a href="http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/com/dropbox/core/DbxClient.html#getFile%28java.lang.String,%20java.lang.String,%20java.io.OutputStream%29">SDK javadoc</a>
     *
     * @param to local path for download.
     * @param auth authenticate finish class has access token
     * @return result of DbxClient.getFile
     */
    def downloadTo(to: String)(implicit auth: DbxAuthFinish) = asDownloadFile(to){ stream =>
      client(auth.accessToken).getFile(path, rev, stream)
    }

    /**
     * remove file on Dropbox for the path.<br/>
     * more detail, see the <a href="http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/com/dropbox/core/DbxClient.html#delete%28java.lang.String%29">SDK javadoc</a><br/>
     * this method has not result.
     *
     * @param auth authenticate finish class has access token
     */
    def remove(implicit auth: DbxAuthFinish) = client(auth.accessToken).delete(path)

    /**
     * copy file to destination path on Dropbox.<br/>
     * more detail, see the <a href="http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/com/dropbox/core/DbxClient.html#copy%28java.lang.String,%20java.lang.String%29">SDK javadoc<a/>
     *
     * @param toPath destination path to file copy
     * @param auth authenticate finish class has access token
     * @return result of DbxClient.copy
     */
    def copyTo(toPath: DropboxPath)(implicit auth: DbxAuthFinish) = client(auth.accessToken).copy(path, toPath.path)

    /**
     * move file to destination path on Dropbox.<br/>
     * more detail, see the <a href="http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/com/dropbox/core/DbxClient.html#move%28java.lang.String,%20java.lang.String%29">SDK javadoc</a>
     *
     * @param toPath destination path to file copy
     * @param auth authenticate finish class has access token
     * @return result of DbxClient.move
     */
    def moveTo(toPath: DropboxPath)(implicit auth: DbxAuthFinish) = client(auth.accessToken).move(path, toPath.path)

    /**
     * create sharable url of receiver path.<br/>
     * more detail, see the <a href="http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/com/dropbox/core/DbxClient.html#createShareableUrl%28java.lang.String%29">SDK javadoc</a>
     *
     * @param auth authenticate finish class has access token
     * @return result of DbxClient.createSharableUrl
     */
    def shareLink(implicit auth: DbxAuthFinish) = client(auth.accessToken).createShareableUrl(path)

    /**
     * create temprary direct url of receiver path. the url created this method will stop working after few hours.<br/>
     * more detail, see the <a href="http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/com/dropbox/core/DbxClient.html#createTemporaryDirectUrl%28java.lang.String%29">SDK javadoc</a>
     *
     * @param auth authenticate finish class has access token
     * @return result of DbxClient.createTemporaryDirectUrl
     */
    def tempDirectLink(implicit auth: DbxAuthFinish) = client(auth.accessToken).createTemporaryDirectUrl(path)
  }

  implicit def MetadataToChildren(metadata: DbxEntry.WithChildren): List[DbxEntry] = metadata.children.toList

  private def asDownloadFile[T](path: String)(f: (FileOutputStream) => T) = {
    val stream = new FileOutputStream(path)

    using(stream, f(stream))
  }

  private def asUploadFile[T](file: File)(f: (File, FileInputStream) => T) = {
    val stream = new FileInputStream(file)

    using(stream, f(file, stream))
  }

  private def using[T](stream: java.io.Closeable, ret: => T) = try {
    ret
  } finally {
    stream.close
  }
}
