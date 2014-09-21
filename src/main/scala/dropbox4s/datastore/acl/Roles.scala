package dropbox4s.datastore.acl

/**
 * @author mao.instantlife at gmail.com
 */
sealed case class Role(role: Int)
object Viewer extends Role(1000)
object Editor extends Role(2000)
object Owner extends Role(3000)
