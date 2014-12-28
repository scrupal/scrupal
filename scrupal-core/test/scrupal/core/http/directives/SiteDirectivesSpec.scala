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

package scrupal.core.http.directives

import org.specs2.mutable.Specification
import scrupal.core.api.{NodeSite, Scrupal}
import scrupal.core.http.SiteDirectives
import scrupal.test.CaseClassFixture
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest


case class Sites(name: String) extends CaseClassFixture[Sites] {
  val scrupal = new Scrupal(name)
  def mkName(int: Int) : Symbol = Symbol(name + int)
  val s1 = NodeSite(mkName(1), "TestSite", "Testing only", "site1", requireHttps=false)
  val s2 = NodeSite(mkName(2), "TestSite", "Testing only", "site2", requireHttps=true)
  val s3 = NodeSite(mkName(3), "TestSite", "Testing only", "site3", requireHttps=false)
  val s4 = NodeSite(mkName(4), "TestSite", "Testing only", "site4", requireHttps=true)
  scrupal.enable(s3)
  scrupal.enable(s4)
}

/** Test Suite For SiteDirectives
 */
class SiteDirectivesSpec extends Specification with Specs2RouteTest with HttpService with SiteDirectives {

  def actorRefFactory = system

  /*

  "site" should {
    /* FIXME: This needs to be rethought as enablement should not be done on a site directly
    "reject disabled site" in {
      Get(Uri("https://site2/")) ~> site { site => complete("works") } ~> check {
        rejection.toString must contain("is disabled")
      }
    }
    */
    "reject http scheme for site with requireHttps" in { Sites("rejectHttp") { sites : Sites ⇒
      Get(Uri("http://site2/")) ~> site(sites.scrupal) { site : Site => complete("works") } ~> check {
        handled === false
      }
    }}
    "reject https scheme for site without requireHttps " in { Sites("rejectHttps") { sites ⇒
      Get(Uri("https://site1")) ~> site(sites.scrupal) { site : Site => complete("works") } ~> check {
        handled === false
      }
    }}
    "reject non-sensical scheme" in { Sites("rejectNonsense") { sites: Sites ⇒
      Get(Uri("ftp://site1")) ~> site(sites.scrupal) { site : Site => complete("works") } ~> check {
        rejections === List(SchemeRejection("http"), SchemeRejection("https"))
      }
    }}
    "accept enabled http site" in { Sites("accpetHttp") { sites : Sites ⇒
      Get("http://site3") ~> site(sites.scrupal) { site : Site => complete("") } ~> check {
        handled === true
      }
    }}
    "accept enabled https site" in { Sites("acceptHttps") { sites: Sites ⇒
      Get("https://site4") ~> site(sites.scrupal) { site : Site => complete("") } ~> check {
        handled === true
      }
    }}
  } */

}
