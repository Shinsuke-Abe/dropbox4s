package dropbox4s.datastore.atom

import org.json4s.JsonDSL._
import org.json4s.JsonAST.JValue

/**
 * This class is special field type of record.
 *
 * dbase64-encoded bytes
 *
 * @author mao.instantlife at gmail.com
 */
case class WrappedBytes(B: String) extends WrappedAtom {
  override def toJsonValue: JValue = ("B" -> B)
}
