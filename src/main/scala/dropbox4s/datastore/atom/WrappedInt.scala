package dropbox4s.datastore.atom

import org.json4s.JsonAST.JValue
import org.json4s.JsonDSL._

/**
 * @author mao.instantlife at gmail.com
 */
case class WrappedInt(I: String) {
  def toJsonValue:JValue = ("I", I)
}
