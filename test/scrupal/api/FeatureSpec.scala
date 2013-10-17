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

package api

import org.specs2.mutable.Specification
import scrupal.api.Feature

/** Test cases for the scrupal.api.Feature class
  * Further description here.
  */
class FeatureSpec extends Specification {

  val on = Feature('TestFeatureOn, "Testing Feature: On", true)
  val off = Feature('TestFeatureOff, "Testing Feature: Off", false)

  "Feature" should {
    "create with three arguments" in {
      on.isEnabled must beTrue
    }
    "access with one argument" in {
      val truth = Feature('TestFeatureOn)
      truth.isDefined must beTrue
      val t = truth.get
      t.isEnabled must beTrue
      val truth2 = Feature('TestFeatureOff)
      truth2.isDefined must beTrue
      val t2 = truth2.get
      t2.isEnabled must beFalse
    }
    "enable and disable on request" in {
      val f = Feature('TestFeature, "Testing Feature", false)
      f.enable()
      f.isEnabled must beTrue
      f.disable()
      f.isEnabled must beFalse
    }
    "convert to boolean implicitly" in {
      Feature.featureToBool(on) must beTrue
      off.isEnabled must beFalse
      if (Feature('TestFeatureOn)) {
        success
      }
      else
      {
        failure
      }
    }
  }
}
