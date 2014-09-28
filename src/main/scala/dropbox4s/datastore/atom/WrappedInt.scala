package dropbox4s.datastore.atom

import org.json4s.JsonDSL._
import org.json4s.JsonAST.JValue

/**
 * This class is special field type of record.
 *
 * decimal representation of a signed 64-bit int
 *
 * @author mao.instantlife at gmail.com
 */
case class WrappedInt(I: String) {
  def toJValue: JValue = ("I" -> I)
}
