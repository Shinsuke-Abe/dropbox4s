package dropbox4s.datastore.atom

import org.json4s.JsonAST.JValue

/**
 * @author mao.instantlife at gmail.com
 */
trait WrappedAtom {
  def toJsonValue: JValue
}
