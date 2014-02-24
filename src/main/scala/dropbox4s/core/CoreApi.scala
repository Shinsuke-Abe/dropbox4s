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

import com.dropbox.core.{DbxEntry, DbxWriteMode, DbxRequestConfig, DbxClient}
import dropbox4s.commons.auth.AccessToken
import java.util.Locale
import dropbox4s.core.model.DropboxPath
import java.io.{File, FileOutputStream, FileInputStream}
import collection.JavaConversions._

/**
 * @author mao.instantlife at gmail.com
 */
trait CoreApi {
  val applicationName: String
  val version: String
  val locale: Locale = Locale.getDefault

  lazy val clientIdentifier = s"${applicationName}/${version} dropbox4s/0.0.1"
  lazy val requestConfig = new DbxRequestConfig(clientIdentifier, locale.toString)

  lazy val client = new DbxClient(requestConfig, _: String)

  def accountInfo(implicit token: AccessToken) = client(token.token).getAccountInfo

  def search(path: DropboxPath, query: String)(implicit token: AccessToken): List[DbxEntry] =
    client(token.token).searchFileAndFolderNames(path.path, query).toList

  def createFolder(path: DropboxPath)(implicit token: AccessToken) =
    client(token.token).createFolder(path.path)

  implicit class DbxRichFile(val localFile: File) {
    def uploadTo(to: DropboxPath, isForce: Boolean = false)(implicit token: AccessToken) = asUploadFile(localFile){ (file, stream) =>
      if(isForce) client(token.token).uploadFile(to.path, DbxWriteMode.force, localFile.length, stream)
      else client(token.token).uploadFile(to.path, DbxWriteMode.add, localFile.length, stream)
    }
  }

  implicit class DbxRichEntryFile(val fileEntity: DbxEntry.File) {
    def update(newFile: File)(implicit token: AccessToken) = asUploadFile(newFile){ (file, stream) =>
      client(token.token).uploadFile(fileEntity.path, DbxWriteMode.update(fileEntity.rev), newFile.length, stream)
    }
  }

  implicit class RichDropboxPath(val dropboxPath: DropboxPath) {
    def children(implicit token: AccessToken) = client(token.token).getMetadataWithChildren(dropboxPath.path)
  }

  implicit def FileEntryToRichPath(fileEntity: DbxEntry.File) = RichPath(fileEntity.path, fileEntity.rev)
  implicit def DropboxPathToRichPath(dropboxPath: DropboxPath) = RichPath(dropboxPath.path)

  case class RichPath(val path: String, val rev: String = null) {
    def downloadTo(to: String)(implicit token: AccessToken) = asDownloadFile(to){ stream =>
      client(token.token).getFile(path, rev, stream)
    }

    def remove(implicit token: AccessToken) = client(token.token).delete(path)

    def copyTo(toPath: DropboxPath)(implicit token: AccessToken) = client(token.token).copy(path, toPath.path)

    def moveTo(toPath: DropboxPath)(implicit token: AccessToken) = client(token.token).move(path, toPath.path)

    def shareLink(implicit token: AccessToken) = client(token.token).createShareableUrl(path)

    def tempDirectLink(implicit token: AccessToken) = client(token.token).createTemporaryDirectUrl(path)
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
