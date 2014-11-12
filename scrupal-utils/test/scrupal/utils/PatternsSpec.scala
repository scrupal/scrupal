package scrupal.utils

import org.specs2.mutable.Specification

import scala.util.matching.Regex

/**
 * Created by reidspencer on 11/10/14.
 */
class PatternsSpec extends Specification {
import Patterns._

  "anchored" should {
    "force a match to limits of input" in {
      val pat = anchored("foo".r)
      pat.pattern.matcher("kafoom").matches must beFalse
      pat.pattern.matcher("foo").matches must beTrue
    }
  }

  "join" should {
    "form a conjunction" in {
      val pat = join("[a-z]+".r,"[0-9]+".r)
      pat.pattern.matcher("abc123").matches must beTrue
      pat.pattern.matcher("abc 123").matches must beFalse
    }
  }

  "atLeastOne" should {
    "match at least one item" in {
      pending
    }
  }

  "zeroOrMore" should {
    "match empty input" in { pending }
    "match multiples input" in { pending }
  }

  "alternate" should {
    "form a disjunction" in { pending }
  }

  "group" should {
    "provide extracted groups" in { pending }
  }

  "between" should {
    "limit the length of the matched text" in { pending }
  }

  "DomainName" should {
    "match legal domain names" in { pending }
    "reject invalid domain names" in { pending }
  }

  "Title" should {
    "not allow non word characters except a few separators and punctuation" in { pending }
  }
}
