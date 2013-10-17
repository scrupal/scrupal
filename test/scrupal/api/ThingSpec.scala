/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2013, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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

/**
 * One line sentence description here.
 * Further description here.
 */
class ThingSpec extends Specification {
  case class TestCreatable(
    override val id: Symbol = Symbol(""),
    override val created: Option[DateTime] = None
  ) extends SymbolicCreatable {
    def blah : Int = 3
  }

  case class Identified[FOO](it: FOO,
    override val id: Option[Long],
    override val created: Option[DateTime] = Some(DateTime.now())
  ) extends NumericCreatable {
    def apply() : FOO = it
  }

  "Creatable" should {
    "report existence with both id and timestamp" in {
      val t = TestCreatable()
      t.exists must beFalse
      val o = new Identified(TestCreatable(),Some(1))
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

  case class TestModifiable(override val modified: Option[DateTime] = Some(DateTime.now()) )
    extends Modifiable

  "Modifiable" should {
    "report modification when changed" in {
      val t = TestModifiable()
      t.isModified must beTrue
    }
    "report non-modification when unchanged" in {
      val t= TestModifiable(None)
      t.isModified must beFalse
    }
  }

  case class TestThing(
    override val name: Symbol,
    override val description: String
  ) extends Thing(name, description) {
  }

  "Thing" should {
    "instantiate with simple arguments" in {
      val t = TestThing('test, "Testing")
      t.isNamed && t.isDescribed must beTrue
    }
  }
}
