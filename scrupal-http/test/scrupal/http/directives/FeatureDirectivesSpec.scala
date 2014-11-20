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

package scrupal.http.directives

import org.specs2.mutable.Specification
import scrupal.api.Feature
import spray.testkit.Specs2RouteTest
import spray.routing.{ValidationRejection, HttpService}

/**
 * Created by reidspencer on 11/5/14.
 */
class FeatureDirectivesSpec extends Specification with Specs2RouteTest with HttpService with FeatureDirectives {

  def actorRefFactory = system

  /* FIXME: This needs to be re-thought as feature enablement is a scope depenent activity
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
