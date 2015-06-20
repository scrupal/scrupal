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

package scrupal.core.http.controllers

import scrupal.core.http.SiteDirectives
import scrupal.test.{ ScenarioGenerator, ScrupalApiSpecification }
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest

/** Test Suite for EntityController */
class EntityControllerSpec extends ScrupalApiSpecification("EntityControllerSpec")
  with Specs2RouteTest with HttpService with SiteDirectives {

  def actorRefFactory = system

  "EntityController" should {
    "compute entity routes sanely" in {
      val sc = ScenarioGenerator("test-EntityRoutes")
      pending("Use ScenarioGenerator")
    }
    "forward a legitimate request" in {
      pending("EntityController not implemented")
    }
    /* FIXME: This is producing false negatives while the same path WORKS when the Scrupal is run
    "handle echo entity deftly" in {
      val scrupal = new Scrupal("handle-echo-entity-deftly")
      scrupal.open()
      val sc = ScenarioGenerator("test-EntityRoutes")

      val ec = new EntityController('ec,0,Site.all(0), Map())
      Get("http://localhost/echo/echo/foo") ~> sealRoute(ec.routes(scrupal)) ~>
      check {
        responseAs[String].contains("Retrieve - foo") must beTrue
        mediaType must beEqualTo(MediaTypes.`text/html`)
      }
      success
    }
    */
  }
}
