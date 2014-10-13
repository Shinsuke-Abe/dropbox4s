package dropbox4s.datastore

/**
 * @author mao.instantlife at gmail.com
 */
package object rules {
  // handle validation rule(less then or equal to 1000, and url-safe base64)
  val DatastoreHandleRule = LessThanOrEqualToRule(1000) << UrlsafeBase64Rule

  // local dsid validation rule
  // - not starts with '.'
  // - and not ends with '.'
  // - and less than or equal to 64
  // - and lowercase letters or digit or '.', '-', '_'
  val LocalDsidRule = RegexNamingRule("""^[^\.].*""") << RegexNamingRule(""".*[^\.]$""") <<
    LessThanOrEqualToRule(64) << RegexNamingRule("""([a-z0-9_\.-])*""")
}
