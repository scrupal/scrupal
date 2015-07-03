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

package scrupal.welcome

import akka.http.scaladsl.model.MediaTypes
import play.api.test.FakeRequest
import scrupal.api._
import scrupal.test.{ProviderTest, ScrupalApiSpecification}

/** Title Of Thing.
  *
  * Description of thing
  */
class WelcomeSiteSpec extends ScrupalApiSpecification("WelcomeSite") with ProviderTest {

  lazy val ws : WelcomeSite = WelcomeSite('welcome)(testScrupal)

  lazy val root = FakeRequest("GET", "/")
  lazy val rootRx = NodeReactor(Node.empty)
  lazy val rootResp = HtmlResponse("Main index page for Welcome To Scrupal Site")
  lazy val doc = FakeRequest("GET", "/doc")
  lazy val docRx = NodeReactor(Node.empty)

  lazy val docResp = HtmlResponse("Documentation")

  s"$specName" should {
    s"route $root to WelcomeSite Root" in
      providerTest(ws, root, rootRx, rootResp) { (reactor: Reactor, response: Response) ⇒
      response.disposition.isSuccessful must beTrue
      response.mediaType must beEqualTo(MediaTypes.`text/html`)
    }
    "route GET:/doc to Documentation Root" in
      providerTest(ws, doc, docRx, docResp) { (reactor: Reactor, response : Response) ⇒
      response.disposition.isSuccessful must beTrue
      response.mediaType must beEqualTo(MediaTypes.`text/html`)
    }
  }
}
