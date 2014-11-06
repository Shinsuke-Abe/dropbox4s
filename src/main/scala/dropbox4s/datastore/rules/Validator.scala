package dropbox4s.datastore.rules

/**
 * @author mao.instantlife at gmail.com
 */
case class Validator[T](targetValue: T) {
  def by(rule: ValidationRule[T]) = rule.check(targetValue)
}
