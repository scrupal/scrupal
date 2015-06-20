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

import scrupal.test.{ScrupalApiSpecification, FakeModule, FakeContext}


/** Test cases for the scrupal.api.Feature class
  * Further description here.
  */
class FeatureSpec extends ScrupalApiSpecification("FeatureSpec") {

  case class Fixture(name: String) extends FakeContext[Fixture](name) {
    val mod = new FakeModule(sym, "Description")
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
