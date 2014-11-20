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

package scrupal.http.controllers

import scrupal.core.Scrupal
import scrupal.api.Site
import scrupal.test.{ScenarioGenerator, ScrupalSpecification}
import scrupal.http.directives.SiteDirectives
import spray.http.MediaTypes
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest

/** Test Suite for EntityController */
class EntityControllerSpec extends ScrupalSpecification("EntityControllerSpec")
                           with Specs2RouteTest with HttpService with SiteDirectives
                            {

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
