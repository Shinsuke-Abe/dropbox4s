package dropbox4s.core.model

/**
 * @author mao.instantlife at gmail.com
 */

import org.specs2.mutable._

class DropboxPathTest extends Specification {
  "DropboxPath#apply" should {
    "create path from param starts with slash" in {
      DropboxPath("/user_path/file.txt").path must equalTo("/user_path/file.txt")
    }

    "throw exception when param not starts with slash" in {
      DropboxPath("user_path/file.txt") must throwA[IllegalArgumentException]
    }
  }

  "DropboxPath#/" should {
    "create new path added child path" in {
      (DropboxPath("/user_path") / "child_path" / "new_file.pdf").path must equalTo("/user_path/child_path/new_file.pdf")
    }
  }

  "DropboxPath#parent" should {
    "get new path has parent path string" in {
      DropboxPath("/user_path/child_path").parent.path must equalTo("/user_path")
    }
  }

  "DropboxPath#name" should {
    "get name from path" in {
      DropboxPath("/user_path/child_path").name must equalTo("child_path")
    }
  }
}
