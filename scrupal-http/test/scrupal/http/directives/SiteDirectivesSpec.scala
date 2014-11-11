package scrupal.http.directives

import org.specs2.mutable.Specification
import scrupal.core.Scrupal
import scrupal.core.api.BasicSite
import spray.testkit.Specs2RouteTest
import spray.routing.{SchemeRejection, HttpService}
import spray.http.Uri

/** Test Suite For SiteDirectives
 */
class SiteDirectivesSpec extends Specification with Specs2RouteTest with HttpService with SiteDirectives {

  def actorRefFactory = system

  var s1 = BasicSite('site1, "TestSite", "Testing only", "site1", requireHttps=false).disable()
  var s2 = BasicSite('site2, "TestSite", "Testing only", "site2", requireHttps=true).disable()
  var s3 = BasicSite('site3, "TestSite", "Testing only", "site3", requireHttps=false)
  var s4 = BasicSite('site4, "TestSite", "Testing only", "site4", requireHttps=true)

  "site" should {
    "reject disabled site" in {
      Get(Uri("https://site2/")) ~> site { site => complete("works") } ~> check {
        rejection.toString must contain("is disabled")
      }
    }
    "reject http scheme for site with requireHttps" in {
      Get(Uri("http://site2/")) ~> site { site => complete("works") } ~> check {
        handled === false
      }
    }
    "reject https scheme for site without requireHttps " in {
      Get(Uri("https://site1")) ~> site { site => complete("works") } ~> check {
        handled === false
      }
    }
    "reject non-sensical scheme" in {
      Get(Uri("ftp://site1")) ~> site { site => complete("works") } ~> check {
        rejections === List(SchemeRejection("http"), SchemeRejection("https"))
      }
    }
    "accept enabled http site" in {
      Get("http://site3") ~> site { site => complete("") } ~> check {
        handled === true
      }
    }
    "accept enabled https site" in {
      Get("https://site4") ~> site { site => complete("") } ~> check {
        handled === true
      }
    }
  }
}
