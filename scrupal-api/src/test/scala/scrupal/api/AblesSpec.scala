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

import java.time.Instant

import org.specs2.mutable.Specification
import scrupal.storage.api.Storable

/**
 * One line sentence description here.
 * Further description here.
 */
class AblesSpec extends Specification {
  case class TestCreatable(
    _id: Symbol = Symbol(""),
    override val created: Option[Instant] = None
  ) extends Creatable with Storable {
    def blah : Int = 3
  }

  case class Identified[FOO](it: FOO,
    _id: Symbol,
    override val created: Option[Instant] = Some(Instant.now())
  ) extends Creatable with Storable {
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
    created: Option[Instant] = Some(Instant.now()),
    modified: Option[Instant] = Some(Instant.now()) )
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
  ) extends Storable with Nameable with Describable {
  }

  "Thing" should {
    "instantiate with simple arguments" in {
      val t = TestThing('test, "Test", "Testing")
      t.isNamed && t.isDescribed must beTrue
    }
  }
}
