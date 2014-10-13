package dropbox4s.datastore.rules

/**
 * @author mao.instantlife at gmail.com
 */

import dropbox4s.commons.DropboxException
import org.apache.commons.codec.binary.Base64
import org.specs2.mutable._

class ValidationRulesTest extends Specification {

  "validation classes" should {
    val stringRuleLengthOverThree = new ValidationRule[String] {
      val check = (target: String) =>
        if(target.length >= 3) target
        else throw DropboxException(s"${target} has not over three characters.")
    }

    "validate value by simple rule, is valid" in {
      (Validator("target string") by stringRuleLengthOverThree) must equalTo("target string")
    }

    "validate value by simple rule, is invalid" in {
      (Validator("hi") by stringRuleLengthOverThree) must
        throwA[DropboxException](message = "hi has not over three characters.")
    }

    val stringRuleIncludeWordTarget = new ValidationRule[String] {
      val check = (target: String) =>
        if(target.contains("target")) target
        else throw DropboxException(s"${target} has not word 'target'.")
    }

    "composite rules, is valid" in {
      (Validator("target string") by (stringRuleLengthOverThree << stringRuleIncludeWordTarget)) must
        equalTo("target string")
    }

    "composite rules, is invalid" in {
      (Validator("new string") by (stringRuleLengthOverThree << stringRuleIncludeWordTarget)) must
        throwA[DropboxException](message = "new string has not word 'target'.")
    }
  }

  "UrlsafeBase64Rule" in {
    "validate string is valid" in {
      val target = Base64.encodeBase64URLSafeString("target string".getBytes)

      (Validator(target) by UrlsafeBase64Rule) must equalTo(target)
    }

    "validation string is invalid, because not url-safe" in {
      val invalidTarget = Base64.encodeBase64String("target string".getBytes)

      (Validator(invalidTarget) by UrlsafeBase64Rule) must
        throwA[DropboxException](message = s"This string is not url-safe Base64 encoding.")
    }

    "validation string is invalid, because not base64 string" in {
      (Validator("target string!") by UrlsafeBase64Rule) must
        throwA[DropboxException](message = "This string is not url-safe Base64 encoding.")
    }
  }

  "LessThenOrEqualToRule" in {
    "validation string is valid" in {
      (Validator("target") by LessThanOrEqualToRule(10)) must equalTo("target")
    }

    "validation string is invalid, because empty string" in {
      (Validator("") by LessThanOrEqualToRule(10)) must
        throwA[DropboxException](message = "Length of string is not less then or equal to")
    }

    "validation string is invalid, because length of string over rule" in {
      (Validator("invalid target") by LessThanOrEqualToRule(10)) must
        throwA[DropboxException](message = "Length of string is not less then or equal to")
    }
  }

  "NotReservedWordRule" in {
    "validation string is valid" in {
      (Validator("target") by NotReservedWordRule) must equalTo("target")
    }

    "validation string is invalid, because string':info' is reserved" in {
      (Validator(":info") by NotReservedWordRule) must
        throwA[DropboxException](message = "This string is reserved by dropbox datastore api.")
    }

    "validation string is invalid, because string':acl' is reserved" in {
      (Validator(":acl") by NotReservedWordRule) must
        throwA[DropboxException](message = "This string is reserved by dropbox datastore api.")
    }
  }

  "RegexNamingRule" in {
    "validation string is valid" in {
      (Validator("valid target") by RegexNamingRule("""^valid .*""")) must equalTo("valid target")
    }

    "validation string is invalid" in {
      (Validator("invalid target") by RegexNamingRule("""^valid .*""")) must
        throwA[DropboxException](message = "This string is not match naming rule.")
    }
  }

  // TODO dsid validation rule(less then or equal to 64, and only available characters, and naming rule)
  // TODO shareable dsid validation rule(less then or equal to to 64, and url-safe base64, and naming rule)
  // TODO handle validation rule(less then or equal to 1000, and url-safe base64)
  // TODO tid or recordid or fieldname validation rule(less then or equal to 64, and available characters, and naming rule)
}
