/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/

package scrupal.utils

import org.specs2.mutable.Specification

import scala.util.matching.Regex

/** Test Suite For Patterns */
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

  // TODO: Finish writing Patterns test cases
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
