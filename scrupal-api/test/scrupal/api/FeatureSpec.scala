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

import scrupal.test.{ScrupalSpecification, FakeContext}


/** Test cases for the scrupal.api.Feature class
  * Further description here.
  */
class FeatureSpec extends ScrupalSpecification("FeatureSpec") {

  case class Fixture(name: String) extends FakeContext[Fixture](name) {
    val mod = new BasicModule(sym, "Description")
    mod.enable(mod)
    assert(mod.isEnabled(mod))
    val omod = Some(mod)

    val impl_on = Feature(sym, "Testing Feature: Enabled_Implemented", omod, implemented = true).enable(mod)
    assert(impl_on.isEnabled(mod))
    val impl_off = Feature(sym, "Testing Feature: Disabled_Implemented", omod, implemented = true)
    assert(!impl_off.isEnabled(mod))
    val unimpl_on = Feature(sym, "Testing Feature: Enabled_Unimplemented", omod, implemented = false).enable(mod)
    assert(!unimpl_on.isEnabled(mod))
    val unimpl_off = Feature(sym, "Testing Feature: Disabled_Unimplemented", omod, implemented = false)
    assert(!unimpl_off.isEnabled(mod))
  }


  "Feature" should {
    "create with three arguments" in Fixture("Create") { f : Fixture ⇒
      val other = Feature('other, "Other", Some(f.mod)).disable(f.mod)
      other.isEnabled(f.mod) must beFalse
      other.implemented must beTrue
    }

    "yield None for undefined Feature" in {
      val maybe = Feature('DoesNotExist)
      maybe must beEqualTo(None)
    }

    "access with one argument" in Fixture("OneArg") { f : Fixture ⇒
      val truth = Feature(f.impl_on.id)
      truth.isDefined must beTrue
      val t1 = truth.get
      t1.isEnabled(f.mod) must beTrue
      val truth2 = Feature(f.impl_off.id)
      truth2.isDefined must beTrue
      val t2 = truth2.get
      t2.isEnabled(f.mod) must beFalse
    }
    "enable and disable on request" in Fixture("EnableDisable") { f: Fixture ⇒
      val tf = Feature('TestFeature, "Testing Feature", f.omod).disable(f.mod)
      tf.isEnabled(f.mod) must beFalse
      tf.enable(f.mod)
      tf.isEnabled(f.mod) must beTrue
      tf.disable(f.mod)
      tf.isEnabled(f.mod) must beFalse
    }
    "convert to boolean implicitly" in Fixture("Convert") { f : Fixture ⇒
      f.impl_off.isEnabled(f.mod) must beFalse
      if (Feature(f.impl_on, f.mod)) {
        success
      }
      else
      {
        failure
      }
    }
  }
}
