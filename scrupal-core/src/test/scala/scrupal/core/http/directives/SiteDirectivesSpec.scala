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

package scrupal.core.http.directives

import org.specs2.mutable.Specification
import scrupal.core.api.Scrupal
import scrupal.core.sites.NodeSite
import scrupal.core.http.SiteDirectives
import scrupal.test.CaseClassFixture
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest

case class Sites(name : String) extends CaseClassFixture[Sites] {
  val scrupal = new Scrupal(name)
  def mkName(int : Int) : Symbol = Symbol(name + int)
  val s1 = NodeSite(mkName(1), "TestSite", "Testing only", "site1", requireHttps = false)
  val s2 = NodeSite(mkName(2), "TestSite", "Testing only", "site2", requireHttps = true)
  val s3 = NodeSite(mkName(3), "TestSite", "Testing only", "site3", requireHttps = false)
  val s4 = NodeSite(mkName(4), "TestSite", "Testing only", "site4", requireHttps = true)
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
      Get(Uri("https://site2/")) ~> site { site ⇒ complete("works") } ~> check {
        rejection.toString must contain("is disabled")
      }
    }
    */
    "reject http scheme for site with requireHttps" in { Sites("rejectHttp") { sites : Sites ⇒
      Get(Uri("http://site2/")) ~> site(sites.scrupal) { site : Site ⇒ complete("works") } ~> check {
        handled === false
      }
    }}
    "reject https scheme for site without requireHttps " in { Sites("rejectHttps") { sites ⇒
      Get(Uri("https://site1")) ~> site(sites.scrupal) { site : Site ⇒ complete("works") } ~> check {
        handled === false
      }
    }}
    "reject non-sensical scheme" in { Sites("rejectNonsense") { sites: Sites ⇒
      Get(Uri("ftp://site1")) ~> site(sites.scrupal) { site : Site ⇒ complete("works") } ~> check {
        rejections === List(SchemeRejection("http"), SchemeRejection("https"))
      }
    }}
    "accept enabled http site" in { Sites("accpetHttp") { sites : Sites ⇒
      Get("http://site3") ~> site(sites.scrupal) { site : Site ⇒ complete("") } ~> check {
        handled === true
      }
    }}
    "accept enabled https site" in { Sites("acceptHttps") { sites: Sites ⇒
      Get("https://site4") ~> site(sites.scrupal) { site : Site ⇒ complete("") } ~> check {
        handled === true
      }
    }}
  } */

}
