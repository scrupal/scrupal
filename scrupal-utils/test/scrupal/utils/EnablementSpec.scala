/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

package scrupal.utils

import org.specs2.mutable.Specification
import scrupal.test.ClassFixture

class Scenario extends AutoCloseable {
  case class TestScope(id: Symbol, override val children : Seq[EnablementScope] = Seq()) extends EnablementScope
  case class TestEnablee(override val parent: Option[Enablee] = None) extends Enablee

  val root_1_a = TestScope('root_1_a )
  val root_1_b = TestScope('root_1_b)
  val root_1 = TestScope('root_1, Seq(root_1_a, root_1_b))
  val root_2_a = TestScope('root_2_a)
  val root_2 = TestScope('root_2, Seq(root_2_a))
  val root = TestScope('root, Seq(root_1, root_2))

  val e_root = TestEnablee()
  val e_root_1 = TestEnablee(Some(e_root))
  val e_root_2 = TestEnablee(Some(e_root))

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
//      s.e_root.isEnabled(s.root_1) must beTrue
      s.root_1_a.isEnabled(s.e_root) must beFalse
//      s.root_1.isEnabled(s.e_root) must beTrue
    }
    "allow enable on multiple scopes" in scenario { s ⇒
      pending
    }
    "allow disable on multiple scopes" in scenario { s ⇒
      pending
    }
    "allow query on multiple scopes" in scenario { s ⇒
      pending
    }
  }
}
