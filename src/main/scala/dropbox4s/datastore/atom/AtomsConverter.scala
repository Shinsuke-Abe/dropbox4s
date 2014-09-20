package dropbox4s.datastore.atom

import java.sql.Timestamp

/**
 * @author mao.instantlife at gmail.com
 */
object AtomsConverter {
  // for wrapped_int
  implicit def wrappedIntToInt(wrappedInt: WrappedInt): Int = wrappedInt.I.toInt

  implicit def wrappedIntToLong(wrappedInt: WrappedInt): Long = wrappedInt.I.toLong

  implicit def IntToWrappedInt(value: Int): WrappedInt = WrappedInt(value.toString)

  implicit def LongToWrappedInt(value: Long): WrappedInt = WrappedInt(value.toString)

  // for wrapped_timestamp
  implicit def wrappedTimestampToTimestamp(wrappedTimestamp: WrappedTimestamp): Timestamp = new Timestamp(wrappedTimestamp.T.toLong)

  implicit def wrappedTimestampToLong(wrappedTimestamp: WrappedTimestamp): Long = wrappedTimestamp.T.toLong

  implicit def timestampToWrappedTimestamp(value: Timestamp): WrappedTimestamp = WrappedTimestamp(value.getTime.toString)

  implicit def longToWrappedTimestamp(value: Long): WrappedTimestamp = WrappedTimestamp(value.toString)
}
