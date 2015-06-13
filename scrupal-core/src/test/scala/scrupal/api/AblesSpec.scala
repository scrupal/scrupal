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

import org.joda.time.DateTime
import org.specs2.mutable.Specification
import scrupal.db.Storable

/**
 * One line sentence description here.
 * Further description here.
 */
class AblesSpec extends Specification {
  case class TestCreatable(
    _id: Symbol = Symbol(""),
    override val created: Option[DateTime] = None
  ) extends Creatable with Storable[Symbol] {
    def blah : Int = 3
  }

  case class Identified[FOO](it: FOO,
    _id: Symbol,
    override val created: Option[DateTime] = Some(DateTime.now())
  ) extends Creatable with Storable[Symbol] {
    def apply() : FOO = it
  }

  "Creatable" should {
    "report existence with both id and timestamp" in {
      val t = TestCreatable()
      t.exists must beFalse
      val o = new Identified(TestCreatable(),'id)
      o.exists must beTrue
      o().blah must beEqualTo(3)
    }

    "report non-existence without id" in {
      val t = TestCreatable()
      t.exists must beFalse
    }

    "report non-existence without timestamp" in {
      val t = TestCreatable()
      t.exists must beFalse
    }
  }

  case class TestModifiable(
    created: Option[DateTime] = Some(DateTime.now()),
    modified: Option[DateTime] = Some(DateTime.now()) )
    extends Modifiable

  "Modifiable" should {
    "report modification when changed" in {
      val t = TestModifiable()
      t.isModified must beTrue
    }
    "report non-modification when unchanged" in {
      val t = TestModifiable(None, None)
      t.isModified must beFalse
    }
  }

  case class TestThing(
    _id: Symbol,
    name: String,
    description: String
  ) extends Storable[Symbol] with Nameable with Describable {
  }

  "Thing" should {
    "instantiate with simple arguments" in {
      val t = TestThing('test, "Test", "Testing")
      t.isNamed && t.isDescribed must beTrue
    }
  }
}
