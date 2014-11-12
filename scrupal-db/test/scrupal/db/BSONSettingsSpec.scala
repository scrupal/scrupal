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

/***********************************************************************************************************************
  * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
  *                                                                                                                    *
  * © 2014, Reactific Systems, Inc. All Rights Reserved.                                                               *
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
package scrupal.db

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
    def apply[R : AsResult](f: BSONSettings => R) = {
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
