package scrupal.http.controllers

import org.specs2.mutable.Specification
import scrupal.http.directives.SiteDirectives
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest

/** Test Suite for EntityController */
class EntityControllerSpec extends Specification with Specs2RouteTest with HttpService with SiteDirectives {

  def actorRefFactory = system

  "EntityController" should {
    "forward a legitimate request" in {
      failure("not implemented")
    }
  }
}
