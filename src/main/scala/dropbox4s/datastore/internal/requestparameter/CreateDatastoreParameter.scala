package dropbox4s.datastore.internal.requestparameter

import java.security.MessageDigest

import org.apache.commons.codec.binary.Base64

/**
 * @author mao.instantlife at gmail.com
 */
case class CreateDatastoreParameter(key: String) {
  require(Option(key).isDefined)

  val messageDigest = MessageDigest.getInstance("SHA-256")

  def dsid = "." + Base64.encodeBase64URLSafeString(messageDigest.digest(key.getBytes))
}
