package scrupal.http.directives

import org.specs2.mutable.Specification
import scrupal.core.api.Feature
import spray.testkit.Specs2RouteTest
import spray.routing.{ValidationRejection, HttpService}

/**
 * Created by reidspencer on 11/5/14.
 */
class FeatureDirectivesSpec extends Specification with Specs2RouteTest with HttpService with FeatureDirectives {

  def actorRefFactory = system

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

}
