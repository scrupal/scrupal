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

package scrupal.api

import org.specs2.mutable.Specification
import scrupal.api.types.RangeType
import scrupal.test.{ScrupalApiSpecification, FakeModule}
import scrupal.utils.Version

import scala.collection.immutable.HashMap

/** Test specifications for the API Module class */
class ModuleSpec extends ScrupalApiSpecification("Module") {

  sequential // FIXME: Garbage collection affects this when run in parallel

  val db = "test-modules"

  object Module1 extends FakeModule('Module1, db, Version(1,0,0), Version(0,8,20)) {
    override val types = Seq(
      new RangeType('Foo, "Fooness", 0, 0)
    )
  }

  object Module2 extends FakeModule('Module2, db, Version(1,0,0), Version(0,9,1)) {
    override val dependencies = Map[Symbol,Version](
      'Module1 -> Version(0,8,21)
    )
  }

  object Module3 extends FakeModule('Module3, db, Version(1,0,0), Version(0,9,1)) {
    override val dependencies = HashMap[Symbol,Version](
      'Module1 -> Version(0,9,10)
    )
  }

  "Version" should {
    "compare correctly with large values" in {
      val v1 = Version(32767, 2112525145, 0)
      val v2 = Version(32767, 2112525146, 0)
      val v3 = Version(0,1,0)
      v1 < v2 must beTrue
      v3 < v2 must beTrue
    }
  }

  "Module1" should {
    "have obsoletes prior to version" in {
      Module1.obsoletes < Module1.version must beTrue
    }
    "have same obsolete Version as Module2's dependency" in {
      Module1.obsoletes == Module2.dependencies('Module1)
    }
    "have different obsolete Version as Module3's dependency" in {
      Module1.obsoletes < Module3.dependencies('Module1)
    }
    "be incompatible with Module2" in {
      Module1.isCompatibleWith(Module2) must beFalse
    }
    "be compatible with Module3" in {
      Module1.isCompatibleWith((Module3)) must beTrue
    }
  }

  "Modules" should {
    "register three modules" in {
      scrupal.Modules('Module1) must beEqualTo(Some(Module1))
      scrupal.Modules('Module2) must beEqualTo(Some(Module2))
      scrupal.Modules('Module3) must beEqualTo(Some(Module3))
    }
  }


}
