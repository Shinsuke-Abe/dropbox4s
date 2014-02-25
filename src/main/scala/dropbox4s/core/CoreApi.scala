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

  def accountInfo(implicit auth: DbxAuthFinish) = client(auth.accessToken).getAccountInfo

  def search(path: DropboxPath, query: String)(implicit auth: DbxAuthFinish): List[DbxEntry] =
    client(auth.accessToken).searchFileAndFolderNames(path.path, query).toList

  def createFolder(path: DropboxPath)(implicit auth: DbxAuthFinish) =
    client(auth.accessToken).createFolder(path.path)

  implicit class DbxRichFile(val localFile: File) {
    def uploadTo(to: DropboxPath, isForce: Boolean = false)(implicit auth: DbxAuthFinish) = asUploadFile(localFile){ (file, stream) =>
      if(isForce) client(auth.accessToken).uploadFile(to.path, DbxWriteMode.force, localFile.length, stream)
      else client(auth.accessToken).uploadFile(to.path, DbxWriteMode.add, localFile.length, stream)
    }
  }

  implicit class DbxRichEntryFile(val fileEntity: DbxEntry.File) {
    def update(newFile: File)(implicit auth: DbxAuthFinish) = asUploadFile(newFile){ (file, stream) =>
      client(auth.accessToken).uploadFile(fileEntity.path, DbxWriteMode.update(fileEntity.rev), newFile.length, stream)
    }
  }

  implicit class RichDropboxPath(val dropboxPath: DropboxPath) {
    def children(implicit auth: DbxAuthFinish) = client(auth.accessToken).getMetadataWithChildren(dropboxPath.path)

    def revisions(implicit auth: DbxAuthFinish) = client(auth.accessToken).getRevisions(dropboxPath.path)
  }

  implicit def FileEntryToRichPath(fileEntity: DbxEntry.File) = RichPath(fileEntity.path, fileEntity.rev)
  implicit def DropboxPathToRichPath(dropboxPath: DropboxPath) = RichPath(dropboxPath.path)

  case class RichPath(val path: String, val rev: String = null) {
    def downloadTo(to: String)(implicit auth: DbxAuthFinish) = asDownloadFile(to){ stream =>
      client(auth.accessToken).getFile(path, rev, stream)
    }

    def remove(implicit auth: DbxAuthFinish) = client(auth.accessToken).delete(path)

    def copyTo(toPath: DropboxPath)(implicit auth: DbxAuthFinish) = client(auth.accessToken).copy(path, toPath.path)

    def moveTo(toPath: DropboxPath)(implicit auth: DbxAuthFinish) = client(auth.accessToken).move(path, toPath.path)

    def shareLink(implicit auth: DbxAuthFinish) = client(auth.accessToken).createShareableUrl(path)

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
