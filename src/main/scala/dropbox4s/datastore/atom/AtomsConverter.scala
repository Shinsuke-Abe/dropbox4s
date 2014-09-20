package dropbox4s.datastore.atom

/**
 * @author mao.instantlife at gmail.com
 */
object AtomsConverter {
  // for wrapped_int
  implicit def wrappedIntToInt(wrappedInt: WrappedInt): Int = wrappedInt.I.toInt

  implicit def wrappedIntToLong(wrappedInt: WrappedInt): Long = wrappedInt.I.toLong

  implicit def IntToWrappedInt(value: Int): WrappedInt = WrappedInt(value.toString)

  implicit def LongToWrappedInt(value: Long): WrappedInt = WrappedInt(value.toString)
}
