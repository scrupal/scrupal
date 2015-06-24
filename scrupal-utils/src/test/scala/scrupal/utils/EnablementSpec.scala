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

import org.specs2.mutable.Specification
import scrupal.test.ClassFixture

class Scenario extends AutoCloseable {
  object TSRegistry extends Registry[TestScope] { val registryName = "TestScopes"; val registrantsName = "test scopes" }
  case class TestScope(id : Symbol, children : Seq[Enablement[_]] = Seq())
    extends Enablement[TestScope] with Registrable[TestScope] {
    def registry : Registry[TestScope] = TSRegistry
    def asT : TestScope = this
    def isChildScope(x : Enablement[_]) : Boolean = children.contains(x)
  }
  object TERegistry extends Registry[TestEnablee] {
    val registryName = "TestEnablees"; val registrantsName = "test enablees"
  }

  case class TestEnablee(id : Symbol, override val parent : Option[Enablee] = None) extends Enablee with Registrable[TestEnablee] {
    def asT : TestEnablee = this
    def registry : Registry[TestEnablee] = TERegistry
  }

  val root_1_a = TestScope('root_1_a)
  val root_1_b = TestScope('root_1_b)
  val root_1 = TestScope('root_1, Seq(root_1_a, root_1_b))
  val root_2_a = TestScope('root_2_a)
  val root_2 = TestScope('root_2, Seq(root_2_a))
  val root = TestScope('root, Seq(root_1, root_2))

  val e_root = TestEnablee('e_root)
  val e_root_1 = TestEnablee('e_root_1, Some(e_root))
  val e_root_2 = TestEnablee('e_root_2, Some(e_root))

  def close() = {

  }
}
/** Test Suite For Enablement */
class EnablementSpec extends Specification {

  val scenario = new ClassFixture(new Scenario)

  "Enablee" should {
    "allow enable on multiple scopes" in scenario { s ⇒
      s.e_root.enable(s.root)
      s.e_root.isEnabled(s.root) must beTrue
      s.e_root.enable(s.root_1)
      s.e_root.isEnabled(s.root_1) must beTrue
    }
    "allow disable on multiple scopes" in scenario { s ⇒
      s.e_root.disable(s.root)
      s.e_root.isEnabled(s.root) must beFalse
      s.e_root.disable(s.root_1)
      s.e_root.isEnabled(s.root_1) must beFalse
    }
    "allow query on arbitrary scopes" in scenario { s ⇒
      s.root.enable(s.e_root, s.root_1)
      s.e_root.isEnabled(s.root) must beFalse
      s.e_root.isEnabled(s.root_1) must beFalse
      s.root.isEnabled(s.e_root, s.root_1) must beTrue
      s.root_1.isEnabled(s.e_root) must beFalse
      s.root_1_a.isEnabled(s.e_root) must beFalse
    }
    "should not be enabled if parent is disabled" in scenario { s ⇒
      s.root.disable(s.e_root)
      s.root.enable(s.e_root_1)
      s.root.isEnabled(s.e_root_1) must beFalse
      s.e_root_1.isEnabled(s.root) must beFalse
    }

  }
}
