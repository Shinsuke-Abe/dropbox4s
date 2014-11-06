package dropbox4s.datastore.rules

import dropbox4s.commons.DropboxException
import org.apache.commons.codec.binary.Base64
import scala.util.control.Exception._

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

  override val check:(String) => String = (target) => {
    if(Base64.isBase64(target) && reg.findAllMatchIn(target).isEmpty) target
    else throw DropboxException(s"This string is not url-safe Base64 encoding. string=${target}")
  }
}

case class LessThanOrEqualToRule(length: Int) extends ValidationRule[String] {
  override val check: (String) => String = (target) => {
    if(!target.isEmpty && target.length <= length) target
    else throw DropboxException(s"Length of string is not less then or equal to ${length}. string=${target}")
  }
}

case class RegexNamingRule(namingRule: String) extends ValidationRule[String] {
  override val check: (String) => String = (target) => {
    if(target.matches(namingRule)) target
    else throw DropboxException(s"This string is not match naming rule. naming rule=${namingRule}, strnig=${target}")
  }
}

/**
 * shareable dsid validation rule
 * - starts with '.'
 * - and less then or equal to to 64
 * - and url-safe base64
 */
object ShareableDsidRule extends ValidationRule[String] {
  val stringStructureRule = RegexNamingRule("""^\..*""") << LessThanOrEqualToRule(64)

  override val check: (String) => String = (target) =>
    allCatch either stringStructureRule.check(target) match {
      case Left(e) => throw e
      case Right(_) => allCatch either UrlsafeBase64Rule.check(target.substring(1)) match {
        case Left(e) => throw e
        case Right(_) => target
      }
    }
}