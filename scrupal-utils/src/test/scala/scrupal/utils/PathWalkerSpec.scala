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

import org.specs2.execute.AsResult
import org.specs2.mutable.Specification
import org.specs2.specification.Fixture

class PathWalkerSpec extends Specification {

  val test_doc = new Fixture[Map[String, Any]] {
    def apply[R : AsResult](f : Map[String, Any] ⇒ R) = {
      val doc = Map[String, Any](
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
      MapSeqPathWalker("foo", Map.empty[String, Any]) must beEqualTo(None)
    }
    "handle an empty path" in {
      MapSeqPathWalker("", Map.empty[String, Any]) must beEqualTo(None)
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
