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

import org.specs2.execute.{Result, AsResult}
import org.specs2.mutable.Specification
import org.specs2.specification.FixtureExample

trait Abstract extends Registrable[Abstract] {
  def id = 'abstract
  def doit() : Unit = { throw new Exception("did it!") }
  def asT : Abstract = this
}

trait TestRegistry extends Registry[Abstract] {
  override def logger_identity = "Abstract-Registry"
  val registryName = logger_identity
  val registrantsName = "Abstract"
}

/**
 * One line sentence description here.
 * Further description here.
 */
class RegistrationSpec extends Specification with FixtureExample[TestRegistry] {

  def fixture[R : AsResult](f: TestRegistry ⇒ R) : Result = {
    val arg = new TestRegistry {}
    AsResult(f(arg))
  }

  "Registration" should {
    "allow Registrable to be mixed in and handle basic registrations" in { test_registry : TestRegistry ⇒

      case class Test(override val id: Symbol) extends Abstract {
        override def registry: TestRegistry = test_registry
        override def asT: Test = this
      }

      val one = new Test('one)
      val two = new Test('two)
      test_registry.size must beEqualTo(2)
      test_registry.unregister(two)
      two.isRegistered must beFalse
      test_registry.size must beEqualTo(1)
      one.unregister()
      test_registry.size must beEqualTo(0)
      one.isRegistered must beFalse
      test_registry.register(one)
      test_registry.register(two)
      test_registry.size must beEqualTo(2)
      test_registry.contains('one) must beTrue
      test_registry.contains('two) must beTrue
      test_registry.getRegistrant('one) must beEqualTo(Some(one))
      test_registry.getRegistrant('two) must beEqualTo(Some(two))
    }

    "ensure abstraction works" in { abstract_registry : TestRegistry ⇒
      class Concrete extends Abstract {
        override def id = 'concrete
        override def registry: TestRegistry = abstract_registry
        override def asT: Concrete = this

      }

      abstract_registry.size must beEqualTo(0)
      val c = new Concrete
      c.doit() must throwA[Exception]
      abstract_registry.size must beEqualTo(1)
      c.id must beEqualTo('concrete)
      abstract_registry.contains('concrete) must beTrue
      abstract_registry.getRegistrant('concrete) must beEqualTo(Some(c))
      c.unregister()
      abstract_registry.size must beEqualTo(0)
      c.register()
      abstract_registry.size must beEqualTo(1)
    }

    "fail if a val is used" in { abstract_registry : TestRegistry ⇒

      class Concrete extends Abstract {
        override val id = 'concrete
        override def registry: TestRegistry = abstract_registry
        override def asT: Concrete = this
      }
      abstract_registry.size must beEqualTo(0)
      new Concrete must throwA[NullPointerException]
    }

    "work with a case class" in { abstract_registry : TestRegistry ⇒
      case class Concrete(override val id : Symbol = 'concrete) extends Abstract {
        override def registry: TestRegistry = abstract_registry
        override def asT: Concrete = this
      }
      abstract_registry.size must beEqualTo(0)
      val c = new Concrete
      abstract_registry.size must beEqualTo(1)
    }

  }
}
