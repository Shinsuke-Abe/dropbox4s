package dropbox4s.datastore.atom

import org.json4s.JsonDSL._
import org.json4s.JsonAST.JValue

/**
 * This class is special field type of record.
 *
 * milliseconds since 1/1/1970 UT
 *
 * @author mao.instantlife at gmail.com
 */
case class WrappedTimestamp(T: String) extends WrappedAtom {
  override def toJsonValue: JValue = ("T" -> T)
}
