package scrupal.http.controllers

import scrupal.core.Scrupal
import scrupal.core.api.Site
import scrupal.fakes.{ScenarioGenerator, ScrupalSpecification}
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
    "handle echo entity deftly" in {
      val scrupal = new Scrupal
      scrupal.beforeStart()
      val sc = ScenarioGenerator("test-EntityRoutes")

      val ec = new EntityController('ec,0,Site.all(0))
      Get("http://localhost/echo/echo/foo") ~> ec.routes(scrupal) ~>
      check {
        mediaType must beEqualTo(MediaTypes.`text/plain`)
        responseAs[String].contains("Retrieve - foo") must beTrue
      }
    }
  }
}
