package dropbox4s.datastore.rules

import dropbox4s.commons.DropboxException
import org.apache.commons.codec.binary.Base64

/**
 * @author mao.instantlife at gmail.com
 */
trait ValidationRule[T] {
  val check: (T) => T

  def <<(otherRule: ValidationRule[T]) = {
    val newCheck = check compose otherRule.check

    new ValidationRule[T] {
      val check = newCheck
    }
  }
}

object UrlsafeBase64Rule extends ValidationRule[String] {
  val reg = """[\+/=]""".r

  override val check:(String) => String = (target: String) => {
    if(Base64.isBase64(target) && reg.findAllMatchIn(target).isEmpty) target
    else throw DropboxException(s"This string is not url-safe Base64 encoding. string=${target}")
  }
}