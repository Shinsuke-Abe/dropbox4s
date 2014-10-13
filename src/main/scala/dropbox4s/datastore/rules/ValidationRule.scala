package dropbox4s.datastore.rules

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
  override val check = (target: String) => {
    target
  }
}