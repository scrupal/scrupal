package scrupal.http.controllers

import scrupal.fakes.{ScenarioGenerator, ScrupalSpecification}
import scrupal.http.directives.SiteDirectives
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
  }
}
