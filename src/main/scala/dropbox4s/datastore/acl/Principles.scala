package dropbox4s.datastore.acl

/**
 * @author mao.instantlife at gmail.com
 */
sealed case class Principle(name: String)

object Public extends Principle("public")
object Team extends Principle("team")