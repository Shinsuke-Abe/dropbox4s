package dropbox4s.datastore.atom

/**
 * @author mao.instantlife at gmail.com
 */
object AtomsConverter {
  implicit def wrappedIntToInt(wrappedInt: WrappedInt): Int = wrappedInt.I.toInt

  implicit def wrappedIntToLong(wrappedInt: WrappedInt): Long = wrappedInt.I.toLong
}
