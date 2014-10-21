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

package scrupal.utils

import org.specs2.mutable.Specification

/**
 * One line sentence description here.
 * Further description here.
 */
class RegistrationSpec extends Specification {

  val abstract_registry = new Registry[Abstract] {
    override def logger_identity = "Abstract-Registry"
    override val registryName = logger_identity
    override val registrantsName = "Abstract"
  }

  trait Abstract extends Registrable[Abstract] {
    def id = 'abstract
    def doit() : Unit = { throw new Exception("did it!") }
    def registry : Registry[Abstract] = {
      abstract_registry
    }
    def asT : Abstract = this
  }

  "Registration" should {
    "allow Registrable to be mixed in and handle basic registrations" in {
      object test_registry extends Registry[Test] {
        override def logger_identity = "Test-Registry"
        override val registryName = logger_identity
        override val registrantsName = "Test-Registrable"
      }

      case class Test(id: Symbol) extends Registrable[Test] {
        override def registry: Registry[Test] = test_registry
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

    "ensure abstraction works" in {
      class Concrete extends Abstract {
        override def id = 'concrete
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

  }
}
