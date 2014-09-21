package dropbox4s.datastore.atom

/**
 * This class is special field type of record.
 *
 * Special value fields.
 *
 * @author mao.instantlife at gmail.com
 */
sealed case class WrappedSpecial(N: String)

// below objects are predefined values.
object Nan extends WrappedSpecial("nan")
object PlusInf extends WrappedSpecial("+inf")
object MinusInf extends WrappedSpecial("-inf")
