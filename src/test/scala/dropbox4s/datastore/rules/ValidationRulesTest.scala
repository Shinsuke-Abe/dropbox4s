package dropbox4s.datastore.rules

/**
 * @author mao.instantlife at gmail.com
 */

import dropbox4s.commons.DropboxException
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
      (Validator("hi") by stringRuleLengthOverThree) must throwA[DropboxException](message = "hi has not over three characters.")
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

    "composit rules, is invalid" in {
      (Validator("new string") by (stringRuleLengthOverThree << stringRuleIncludeWordTarget)) must
        throwA[DropboxException](message = "new string has not word 'target'.")
    }
  }

  // TODO dsid validation rule
  // TODO shareable dsid validation rule
  // TODO handle validation rule
  // TODO recordid or fieldname validation rule
}
