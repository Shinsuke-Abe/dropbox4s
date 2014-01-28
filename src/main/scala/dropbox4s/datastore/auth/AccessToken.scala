package dropbox4s.datastore.auth

/**
 * @author mao.instantlife at gmail.com
 */
case class AccessToken(token: String) {
  require(Option(token).isDefined && !token.isEmpty)
}
