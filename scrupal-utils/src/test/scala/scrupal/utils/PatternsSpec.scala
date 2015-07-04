/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
 *                                                                                                                    *
 * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
 * with the License. You may obtain a copy of the License at                                                          *
 *                                                                                                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                                                                     *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
 * the specific language governing permissions and limitations under the License.                                     *
 **********************************************************************************************************************/

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

  "Markdown" should {
    val pat = Patterns.Markdown
    "accept 'a'" in {
      pat.pattern.matcher("a").matches must beTrue
    }
    "accept '#'" in {
      pat.pattern.matcher("#").matches must beTrue
    }
    "accept '*'" in {
      pat.pattern.matcher("#").matches must beTrue
    }
  }

  "DomainName" should {
    val pat = Patterns.DomainName
    "match legal domain names" in {
      pat.pattern.matcher("scrupal.org").matches must beTrue
    }
    "reject invalid domain names" in {
      pat.pattern.matcher("a").matches must beFalse

    }
  }

  "Title" should {
    "not allow non word characters except a few separators and punctuation" in {
      Patterns.Title.pattern.matcher("#@$%!@%!@$#@#%)(*&)(").matches must beFalse
    }
    "allow word characters" in {
      Patterns.Title.pattern.matcher("This IS A Title").matches must beTrue

    }
  }
}
