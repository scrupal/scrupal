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

package scrupal.core.http.directives

import org.specs2.mutable.Specification
import scrupal.core.akkahttp.FeatureDirectives

/** Created by reidspencer on 11/5/14.
  */
class FeatureDirectivesSpec extends Specification with FeatureDirectives {


  /* FIXME: This needs to be re-thought as feature enablement is a scope dependent activity
  var f1 = Feature('disabled_unimnplemented, "Feature", false).disable()
  var f2 = Feature('disabled_imnplemented, "Feature", true).disable()
  var f3 = Feature('enabled_unimnplemented, "Feature", false)
  var f4 = Feature('enabled_imnplemented, "Feature", true)

  "feature" should {
    "reject disabled and unimplemented features" in {
      Get() ~> feature(f1) { complete("works") } ~> check {
        rejection.toString must contain("is not implemented")
      }
    }
    "reject disabled but implemented features" in {
      Get() ~> feature(f2) { complete("works") } ~> check {
        rejection.toString must contain("is not enabled")
      }
    }
    "reject enabled but unimplemented feature" in {
      Get() ~> feature(f3) { complete("works") } ~> check {
        rejection.toString must contain("is not implemented")
      }
    }
    "accept enabled implemented features" in {
      Get() ~> feature(f4) { complete("works") } ~> check {
        responseAs[String] must contain("works")
      }
    }
  }
*/
}
