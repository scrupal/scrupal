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

import org.specs2.execute.{ Result, AsResult }
import org.specs2.mutable.Specification
import org.specs2.specification.{ ForEach, FixtureExample }

trait Abstract extends Registrable[Abstract] {
  def doit() : Unit = { throw new Exception("did it!") }
}

trait TestRegistry extends Registry[Abstract] {
  override def logger_identity = "Abstract-Registry"
  val registryName = logger_identity
  val registrantsName = "Abstract"
}

/** One line sentence description here.
  * Further description here.
  */
class RegistrationSpec extends Specification with ForEach[TestRegistry] {

  override protected def foreach[R](f : (TestRegistry) ⇒ R)(implicit evidence$3 : AsResult[R]) : Result = {
    val arg = new TestRegistry {}
    AsResult(f(arg))
  }

  "Registration" should {
    "allow Registrable to be mixed in and handle basic registrations" in { test_registry : TestRegistry ⇒

      case class Test(override val id : Symbol) extends Abstract {
        override def registry : TestRegistry = test_registry
        // override def asT: Test = this
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
      class Concrete extends { override val id = 'concrete } with Abstract {
        override def registry : TestRegistry = abstract_registry
        // override def asT: Concrete = this

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

    "succeed if a lazy val is used" in { abstract_registry : TestRegistry ⇒
      class Concrete extends { override val id = 'concrete } with Abstract {
        override def registry : TestRegistry = abstract_registry
        // override def asT: Concrete = this
      }
      abstract_registry.size must beEqualTo(0)
      try { new Concrete; success } catch {
        case x : Throwable ⇒ failure("Exception should not have been thrown, but got: " + x.getClass.getName)
      }
    }

    "succeed if a lazy val is used in a concrete subclass" in {
      class ConcreteRegistry extends Registry[Concrete] {
        override def logger_identity = "Concrete-Registry"
        val registryName = logger_identity
        val registrantsName = "Concrete"
      }
      lazy val concrete_registry = new ConcreteRegistry
      case class Concrete(id : Symbol) extends {} with Registrable[Concrete] {
        def registry = concrete_registry
        def doit() : Unit = { throw new Exception("did it!") }
      }
      try {
        val c = Concrete('concrete)
        c.id must beEqualTo('concrete)
        concrete_registry.contains('concrete) must beTrue
        success
      } catch {
        case x : Throwable ⇒ failure("Exception should not have been thrown, but got: " + x.getClass.getName)
      }
    }

    "succeed if a val is used in a concrete subclass" in {
      class ConcreteRegistry extends Registry[Concrete] {
        override def logger_identity = "Concrete-Registry"
        val registryName = logger_identity
        val registrantsName = "Concrete"
      }
      lazy val concrete_registry = new ConcreteRegistry
      case class Concrete() extends { val id = 'concrete } with Registrable[Concrete] {
        def registry = concrete_registry
        def doit() : Unit = { throw new Exception("did it!") }
      }
      try {
        val c = Concrete()
        c.id must beEqualTo('concrete)
        concrete_registry.contains('concrete) must beTrue
        success
      } catch {
        case x : Throwable ⇒ failure("Exception should not have been thrown, but got: " + x.getClass.getName)
      }
    }

    "work with a case class" in { abstract_registry : TestRegistry ⇒
      case class Concrete(override val id : Symbol = 'concrete) extends Abstract {
        override def registry : TestRegistry = abstract_registry
        // override def asT: Concrete = this
      }
      abstract_registry.size must beEqualTo(0)
      val c = new Concrete
      abstract_registry.size must beEqualTo(1)
      c.id must beEqualTo('concrete)
      abstract_registry.contains('concrete) must beTrue
    }

  }

}
