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

package scrupal.api

import org.specs2.mutable.Specification
import scrupal.core.RangeType
import scrupal.test.FakeModule
import scrupal.utils.Version

import scala.collection.immutable.HashMap

/** Test specifications for the API Module class */
class ModuleSpec extends Specification {

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
      Module.bootstrap(Seq.empty[String])
      Module('Module1) must beEqualTo(Some(Module1))
      Module('Module2) must beEqualTo(Some(Module2))
      Module('Module3) must beEqualTo(Some(Module3))
    }
  }
}
