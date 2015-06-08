/** ********************************************************************************************************************
  * Copyright © 2014 Reactific Software, Inc.                                                                          *
  *                                                                                                                 *
  * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
  *                                                                                                                 *
  * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
  * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
  * or (at your option) any later version.                                                                             *
  *                                                                                                                 *
  * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
  * details.                                                                                                           *
  *                                                                                                                 *
  * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
  * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
  * ********************************************************************************************************************
  */

package scrupal.utils

import org.specs2.mutable.Specification

import scrupal.utils.Patterns._

/** Test Suite For Patterns */
class PatternsSpec extends Specification {

  "anchored" should {
    "force a match to limits of input" in {
      val pat = anchored("foo".r)
      pat.pattern.matcher("kafoom").matches must beFalse
      pat.pattern.matcher("foo").matches must beTrue
    }
  }

  "join" should {
    "form a conjunction" in {
      val pat = join("[a-z]+".r, "[0-9]+".r)
      pat.pattern.matcher("abc123").matches must beTrue
      pat.pattern.matcher("abc 123").matches must beFalse
    }
  }

  "atLeastOne" should {
    "match at least one item" in {
      val pat = atLeastOne("[a-z]".r)
      pat.pattern.matcher("").matches must beFalse
      pat.pattern.matcher("a").matches must beTrue
      pat.pattern.matcher("bcd").matches must beTrue
    }
  }

  "zeroOrMore" should {
    "match zer or more of a pattern" in {
      val pat = zeroOrMore("[a-z]".r)
      pat.pattern.matcher("").matches must beTrue
      pat.pattern.matcher("a").matches must beTrue
      pat.pattern.matcher("bcd").matches must beTrue
    }
  }

  "alternate" should {
    "form a disjunction" in {
      val pat = alternate("[a-z]+".r, "[A-Z]+".r)
      pat.pattern.matcher("a").matches must beTrue
      pat.pattern.matcher("A").matches must beTrue
      pat.pattern.matcher("bcd").matches must beTrue
      pat.pattern.matcher("BCD").matches must beTrue
      pat.pattern.matcher("AbCd").matches must beFalse
    }
  }

  "group" should {
    "concatenate patterns" in {
      val pat = group("[A]".r, "[a]".r, "[B]".r)
      pat.pattern.matcher("AaB").matches must beTrue
      pat.pattern.matcher("aAb").matches must beFalse
    }
  }

  "capture" should {
    "provide extracted capture groups" in {
      val pat = capture("[a-z]+".r)
      pat.pattern.matcher("A").matches must beFalse
      val m = pat.pattern.matcher("a")
      m.matches must beTrue
      m.groupCount must beEqualTo(1)
      m.group(0) must beEqualTo("a")
    }
  }

  "between" should {
    "limit the length of the matched text" in {
      val pat = scrupal.utils.Patterns.between(2, 5, "[a-z]".r)
      pat.pattern.matcher("a").matches must beFalse
      pat.pattern.matcher("ab").matches must beTrue
      pat.pattern.matcher("abcde").matches must beTrue
      pat.pattern.matcher("abcdef").matches must beFalse
      pat.pattern.matcher("").matches must beFalse
    }

  }

  // TODO: Finish writing Patterns test cases

  "DomainName" should {
    "match legal domain names" in { pending }
    "reject invalid domain names" in { pending }
  }

  "Title" should {
    "not allow non word characters except a few separators and punctuation" in { pending }
  }
}
