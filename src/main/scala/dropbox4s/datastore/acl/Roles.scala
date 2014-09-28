package dropbox4s.datastore.acl

import dropbox4s.datastore.atom.WrappedInt
import dropbox4s.datastore.atom.AtomsConverter._

/**
 * @author mao.instantlife at gmail.com
 */
case class Role(role: WrappedInt)

object Viewer extends Role(1000)
object Editor extends Role(2000)
object Owner extends Role(3000)
