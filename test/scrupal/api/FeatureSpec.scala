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
import scrupal.api.{WithFeature, Feature}
import play.api.mvc.SimpleResult
import play.api.mvc.Results._
import play.mvc.{Http, Results}
import play.api.http
import scrupal.controllers.Context
import scrupal.fakes.WithFakeScrupal

/** Test cases for the scrupal.api.Feature class
  * Further description here.
  */
class FeatureSpec extends Specification {

  val impl_on = Feature(true, 'ImplementedOn, "Testing Feature: Implemented/On", true)
  val impl_off = Feature(true, 'ImplementedOff, "Testing Feature: Implemented/Off", false)
  val unimpl_on = Feature(false, 'UnimplementedOn, "Testing Feature: Unimplemented/On", true)
  val unimpl_off = Feature(false, 'UnimplementedOff, "Testing Feature: Unimplemented/Off", false)

  "Feature" should {
    "create with three arguments" in {
      val other = Feature('other, "Other", /*enabled=*/false)
      other.isEnabled must beFalse
      other.implemented must beTrue
    }
    "yield None for undefined Feature" in {
      val maybe = Feature('DoesNotExist)
      maybe.isDefined must beFalse
    }
    "access with one argument" in {
      val truth = Feature('ImplementedOn)
      truth.isDefined must beTrue
      val t = truth.get
      t.isEnabled must beTrue
      val truth2 = Feature('ImplementedOff)
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
      Feature.featureToBool(impl_on) must beTrue
      impl_off.isEnabled must beFalse
      if (Feature('ImplementedOn)) {
        success
      }
      else
      {
        failure
      }
    }
  }

  "WithFeature" should {
    "Pass through for implemented/enabled" in new WithFakeScrupal {
      implicit val context = Context()
      val result: SimpleResult = WithFeature(impl_on) { Ok("foo") }
      result.header.status must equalTo(http.Status.OK)
    }
    "Generate Redirect for implemented/disabled" in new WithFakeScrupal {
      implicit val context = Context()
      val result: SimpleResult = WithFeature(impl_off) { Ok("foo") }
      result.header.status must equalTo(http.Status.SEE_OTHER)
    }
    "Generate NotImplemented for unimplemented/enabled" in new WithFakeScrupal {
      implicit val context = Context()
      val result: SimpleResult = WithFeature(unimpl_on) { Ok("foo") }
      result.header.status must equalTo(http.Status.NOT_IMPLEMENTED)
    }
    "Generate NotImplemented for unimplemented/disabled" in new WithFakeScrupal {
      implicit val context = Context()
      val result: SimpleResult = WithFeature(unimpl_off) { Ok("foo") }
      result.header.status must equalTo(http.Status.NOT_IMPLEMENTED)
    }
  }
}
