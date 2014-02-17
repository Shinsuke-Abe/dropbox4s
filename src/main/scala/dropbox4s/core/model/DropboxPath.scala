package dropbox4s.core.model

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

/**
 * @author mao.instantlife at gmail.com
 */
case class DropboxPath(path: String) {
  import com.dropbox.core.DbxPath._
  require(isValid(path))

  def /(child: String) = DropboxPath(path + "/" + child)

  def parent = DropboxPath(getParent(path))

  def name = getName(path)
}
