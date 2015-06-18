/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
 *                                                                                                                    *
 * Copyright © 2015 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
 * except in compliance with the License. You may obtain a copy of the License at                                     *
 *                                                                                                                    *
 *        http://www.apache.org/licenses/LICENSE-2.0                                                                  *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the                          *
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
 * either express or implied. See the License for the specific language governing permissions                         *
 * and limitations under the License.                                                                                 *
 **********************************************************************************************************************/

package scrupal.store.reactivemongo

import java.util.concurrent.TimeUnit

import org.specs2.execute.AsResult
import org.specs2.mutable.Specification
import org.specs2.specification.Fixture
import reactivemongo.bson._

import scala.concurrent.duration.Duration

class BSONSettingsSpec extends Specification {

  "BSONPathWalker" should {
    "handle an empty document" in {
      BSONPathWalker("foo",BSONDocument()) must beEqualTo(None)
    }
    "handle an empty path" in {
      BSONPathWalker("", BSONDocument()) must beEqualTo(None)
    }
    "handle element not found" in {
      BSONPathWalker("foo", BSONDocument("bar" → BSONInteger(42))) must beEqualTo(None)
    }
    "find an element at the top level" in {
      BSONPathWalker("foo", BSONDocument("foo" → BSONInteger(42))) must beEqualTo(Some(BSONInteger(42)))
    }
    "find an element at a nested level" in {
      BSONPathWalker("foo.bar",
        BSONDocument("foo" → BSONDocument("bar" → BSONInteger(42)))) must beEqualTo(Some(BSONInteger(42)))
    }
    "find an indexed element at top level" in {
      BSONPathWalker("foo[1]",
        BSONDocument("foo" → BSONArray(BSONInteger(21), BSONInteger(42)))) must beEqualTo(Some(BSONInteger(42)))
    }
  }

  val test_doc = new Fixture[BSONSettings] {
    def apply[R : AsResult](f: BSONSettings ⇒ R) = {
      val doc = BSONDocument(
        "string" → BSONString("This is a string"),
        "false" → BSONBoolean(value = false),
        "true" → BSONBoolean(value = true),
        "int" → BSONInteger(42),
        "long" → BSONLong(42L),
        "double" → BSONDouble(42.0),
        "number_long" → BSONLong(42L),
        "number_int" → BSONInteger(42),
        "number_double" → BSONDouble(42.0),
        "duration" → BSONLong(1415723805000000000L)
      )
      AsResult(f(BSONSettings(doc)))
    }
  }

  "BSONSettings" should {
    "get a string from top level" in test_doc { doc ⇒
      doc.getString("string") must beEqualTo(Some("This is a string"))
    }
    "get a boolean from top level" in test_doc { doc ⇒
      doc.getBoolean("false") must beEqualTo(Some(false))
      doc.getBoolean("true") must beEqualTo(Some(true))
    }
    "get an integer from top level" in test_doc { doc ⇒
      doc.getInt("int") must beEqualTo(Some(42))
    }
    "get a long from top level" in test_doc { doc ⇒
      doc.getLong("long") must beEqualTo(Some(42L))
    }
    "get a double from top level" in test_doc { doc ⇒
      doc.getDouble("double") must beEqualTo(Some(42.0))
    }
    "get a number for a long from top level" in test_doc { doc ⇒
      doc.getNumber("number_long") must beEqualTo(Some(42L))
    }
    "get a number for an int from top level" in test_doc { doc ⇒
      doc.getNumber("number_int") must beEqualTo(Some(42))
    }
    "get a number for a double from top level" in test_doc { doc ⇒
      doc.getNumber("number_double") must beEqualTo(Some(42.0))
    }
    "get a duration from top level" in test_doc { doc ⇒
      doc.getDuration("duration") must beEqualTo(Some(Duration(1415723805L, TimeUnit.SECONDS)))
    }
    "get milliseconds from top level" in test_doc { doc ⇒
      doc.getMilliseconds("duration") must beEqualTo(Some(1415723805000L))
    }
    "get microseconds from top level" in test_doc { doc ⇒
      doc.getMicroseconds("duration") must beEqualTo(Some(1415723805000000L))
    }
    "get nanoseconds from top level" in test_doc { doc ⇒
      doc.getNanoseconds("duration") must beEqualTo(Some(1415723805000000000L))
    }
  }
}
