/** *********************************************************************************************************************
  * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
  * *
  * © Copyright 2014 Reactific Systems, Inc. All Rights Reserved.                                                               *
  * *
  * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
  * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
  * or (at your option) any later version.                                                                             *
  * *
  * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
  * details.                                                                                                           *
  * *
  * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
  * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
  * *********************************************************************************************************************/

package scrupal.utils

import org.specs2.execute.AsResult
import org.specs2.mutable.Specification
import org.specs2.specification.Fixture

class PathWalkerSpec extends Specification {

  val test_doc = new Fixture[Map[String,Any]] {
    def apply[R : AsResult](f: Map[String,Any] => R) = {
      val doc = Map(
        "string" → "This is a string",
        "false" → false,
        "true" → true,
        "int" → 42,
        "long" → 42L,
        "double" → 42.0,
        "number_long" → 42L,
        "number_int" → 42,
        "number_double" → 42.0,
        "duration" → 1415723805000000000L
      )
      AsResult(f(doc))
    }
  }

  "PathWalker" should {
    "handle an empty document" in {
      MapSeqPathWalker("foo",Map.empty[String,Any]) must beEqualTo(None)
    }
    "handle an empty path" in {
      MapSeqPathWalker("", Map.empty[String,Any]) must beEqualTo(None)
    }
    "handle element not found" in {
      MapSeqPathWalker("foo", Map("bar" → 42)) must beEqualTo(None)
    }
    "find an element at the top level" in {
      MapSeqPathWalker("foo", Map("foo" → 42)) must beEqualTo(Some(42))
    }
    "find an element at a nested level" in {
      MapSeqPathWalker("foo.bar",
        Map("foo" → Map("bar" → 42))) must beEqualTo(Some(42))
    }
    "find an indexed element at top level" in {
      MapSeqPathWalker("foo[1]",
        Map("foo" → Seq(21, 42))) must beEqualTo(Some(42))
    }
    "find a variety of types in a Map" in test_doc { doc ⇒
      MapSeqPathWalker("string", doc) must beEqualTo(Some("This is a string"))
      MapSeqPathWalker("false", doc) must beEqualTo(Some(false))
      MapSeqPathWalker("true", doc) must beEqualTo(Some(true))
      MapSeqPathWalker("int", doc) must beEqualTo(Some(42))
      MapSeqPathWalker("long", doc) must beEqualTo(Some(42L))
      MapSeqPathWalker("double", doc) must beEqualTo(Some(42.0))
      MapSeqPathWalker("number_long", doc) must beEqualTo(Some(42L))
      MapSeqPathWalker("number_int", doc) must beEqualTo(Some(42))
      MapSeqPathWalker("number_double", doc) must beEqualTo(Some(42.0))
      MapSeqPathWalker("duration", doc) must beEqualTo(Some(1415723805000000000L))
    }
  }
}
