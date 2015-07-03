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

package scrupal.core.providers

import play.api.test.FakeRequest
import scrupal.api.{Reactor, Response, NoopResponse}
import scrupal.test.{ProviderTest, ScrupalSpecification}

class APIDocSpec extends ScrupalSpecification("APIDoc") with ProviderTest {

  lazy val apidoc = APIDoc()

  lazy val getNumDoc = FakeRequest("GET", "/apidoc/foo/GET/1/facet/facet_id/rest")
  lazy val getNumDocModel = apidoc.GetNumDoc("foo", 1, "facet", "facet_id", "/rest")
  lazy val getNumDocResp = NoopResponse
  lazy val getNamDoc = FakeRequest("GET", "/apidoc/foo/GET/name/facet/facet_id/rest")
  lazy val getNamDocModel = apidoc.GetNameDoc("foo", "name", "facet", "facet_id", "/rest")
  lazy val getNamDocResp = NoopResponse
  lazy val facetInfoNumDoc = FakeRequest("GET", "/apidoc/foo/HEAD/1/facet/facet_id/rest")
  lazy val facetInfoNumModel = apidoc.FacetInfoNumDoc("foo", 1, "facet", "facet_id", "/rest")
  lazy val facetInfoNumResp  = NoopResponse
  lazy val facetInfoNamDoc = FakeRequest("GET", "/apidoc/foo/HEAD/name/facet/facet_id/rest")
  lazy val facetInfoNamModel = apidoc.FacetInfoNameDoc("foo", "name", "facet", "facet_id", "/rest")
  lazy val facetInfoNamResp  = NoopResponse
  lazy val findNumDoc = FakeRequest("GET", "/apidoc/foo/OPTIONS/1/facet/rest")
  lazy val findNumModel = apidoc.FindNumDoc("foo", 1, "facet", "/rest")
  lazy val findNumResp  = NoopResponse
  lazy val findNamDoc = FakeRequest("GET", "/apidoc/foo/OPTIONS/name/facet/rest")
  lazy val findNamModel = apidoc.FindNameDoc("foo", "name", "facet", "/rest")
  lazy val findNamResp  = NoopResponse
  lazy val addNumDoc = FakeRequest("GET", "/apidoc/foo/POST/1/facet/rest")
  lazy val addNumModel = apidoc.AddNumDoc("foo", 1, "facet", "/rest")
  lazy val addNumResp  = NoopResponse
  lazy val addNamDoc = FakeRequest("GET", "/apidoc/foo/POST/name/facet/rest")
  lazy val addNamModel = apidoc.AddNameDoc("foo", "name", "facet", "/rest")
  lazy val addNamResp  = NoopResponse
  lazy val setNumDoc = FakeRequest("GET", "/apidoc/foo/PUT/1/facet/facet_id/rest")
  lazy val setNumModel = apidoc.SetNumDoc("foo", 1, "facet", "facet_id", "/rest")
  lazy val setNumResp = NoopResponse
  lazy val setNamDoc = FakeRequest("GET", "/apidoc/foo/PUT/name/facet/facet_id/rest")
  lazy val setNamModel = apidoc.SetNameDoc("foo", "name", "facet", "facet_id", "/rest")
  lazy val setNamResp = NoopResponse
  lazy val removeNumDoc = FakeRequest("GET", "/apidoc/foo/DELETE/1/facet/facet_id/rest")
  lazy val removeNumModel = apidoc.RemoveNumDoc("foo", 1, "facet", "facet_id", "/rest")
  lazy val removeNumResp = NoopResponse
  lazy val removeNamDoc = FakeRequest("GET", "/apidoc/foo/DELETE/name/facet/facet_id/rest")
  lazy val removeNamModel = apidoc.RemoveNameDoc("foo", "name", "facet", "facet_id", "/rest")
  lazy val removeNamResp = NoopResponse
  lazy val retrieveNumDoc= FakeRequest("GET", "/apidoc/foo/GET/1/rest")
  lazy val retrieveNumModel = apidoc.RetrieveNumDoc("foo", 1, "/rest")
  lazy val retrieveNumResp = NoopResponse
  lazy val retrieveNamDoc = FakeRequest("GET", "/apidoc/foo/GET/name/rest")
  lazy val retrieveNamModel = apidoc.RetrieveNameDoc("foo", "name", "/rest")
  lazy val retrieveNamResp = NoopResponse
  lazy val infoNumDoc = FakeRequest("GET", "/apidoc/foo/HEAD/1/rest")
  lazy val infoNumModel = apidoc.InfoNumDoc("foo", 1, "/rest")
  lazy val infoNumResp = NoopResponse
  lazy val infoNamDoc = FakeRequest("GET", "/apidoc/foo/HEAD/name/rest")
  lazy val infoNamModel = apidoc.InfoNameDoc("foo", "name", "/rest")
  lazy val infoNamResp = NoopResponse
  lazy val queryDoc = FakeRequest("GET", "/apidoc/foo/OPTIONS/rest")
  lazy val queryModel = apidoc.QueryDoc("foo", "/rest")
  lazy val queryResp = NoopResponse
  lazy val createDoc = FakeRequest("GET", "/apidoc/foo/POST/rest")
  lazy val createModel = apidoc.CreateDoc("foo", "/rest")
  lazy val createResp = NoopResponse
  lazy val updateNumDoc = FakeRequest("GET", "/apidoc/foo/PUT/1/rest")
  lazy val updateNumModel = apidoc.UpdateNumDoc("foo", 1, "/rest")
  lazy val updateNumResp = NoopResponse
  lazy val updateNamDoc = FakeRequest("GET", "/apidoc/foo/PUT/name/rest")
  lazy val updateNamModel = apidoc.UpdateNameDoc("foo", "name", "/rest")
  lazy val updateNamResp = NoopResponse
  lazy val deleteNumDoc = FakeRequest("GET", "/apidoc/foo/DELETE/1/rest")
  lazy val deleteNumModel = apidoc.DeleteNumDoc("foo", 1, "/rest")
  lazy val deleteNumResp = NoopResponse
  lazy val deleteNamDoc = FakeRequest("GET", "/apidoc/foo/DELETE/name/rest")
  lazy val deleteNamModel = apidoc.DeleteNameDoc("foo", "name", "/rest")
  lazy val deleteNamResp = NoopResponse
  lazy val entityIntroDoc = FakeRequest("GET", "/apidoc/foo")
  lazy val entityIntroModel = apidoc.EntityIntroduction("foo")
  lazy val entityIntroResp = NoopResponse
  lazy val introductionDoc = FakeRequest("GET", "/apidoc")
  lazy val introductionModel = apidoc.ApiDocIntroduction()
  lazy val introductionResp = NoopResponse

  "APIDoc" should {
    s"generate proper Reactor for $getNumDoc" in
      providerTest(apidoc, getNumDoc, getNumDocModel, getNumDocResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }
    s"generate proper Reactor for $getNamDoc" in
      providerTest(apidoc, getNamDoc, getNamDocModel, getNamDocResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }
    s"generate proper Reactor for $facetInfoNumDoc" in
      providerTest(apidoc, facetInfoNumDoc, facetInfoNumModel, facetInfoNumResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
    }

    s"generate proper Reactor for $facetInfoNamDoc" in
      providerTest(apidoc, facetInfoNamDoc, facetInfoNamModel, facetInfoNamResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }

    s"generate proper Reactor for $findNumDoc" in
      providerTest(apidoc, findNumDoc, findNumModel, findNumResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }

    s"generate proper Reactor for $findNamDoc" in
      providerTest(apidoc, findNamDoc, findNamModel, findNamResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }

    s"generate proper Reactor for $addNumDoc" in
      providerTest(apidoc, addNumDoc, addNumModel, addNumResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }

    s"generate proper Reactor for $addNamDoc" in
      providerTest(apidoc, addNamDoc, addNamModel, addNamResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }

    s"generate proper Reactor for $setNumDoc" in
      providerTest(apidoc, setNumDoc, setNumModel, setNumResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }

    s"generate proper Reactor for $setNamDoc" in
      providerTest(apidoc, setNamDoc, setNamModel, setNamResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }

    s"generate proper Reactor for $removeNumDoc" in
      providerTest(apidoc, removeNumDoc, removeNumModel, removeNumResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }

    s"generate proper Reactor for $removeNamDoc" in
      providerTest(apidoc, removeNamDoc, removeNamModel, removeNamResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }

    s"generate proper Reactor for $retrieveNumDoc" in
      providerTest(apidoc, retrieveNumDoc, retrieveNumModel, retrieveNumResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }

    s"generate proper Reactor for $retrieveNamDoc" in
      providerTest(apidoc, retrieveNamDoc, retrieveNamModel, retrieveNamResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }

    s"generate proper Reactor for $infoNumDoc" in
      providerTest(apidoc, infoNumDoc, infoNumModel, infoNumResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }

    s"generate proper Reactor for $infoNamDoc" in
      providerTest(apidoc, infoNamDoc, infoNamModel, infoNamResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }

    s"generate proper Reactor for $queryDoc" in
      providerTest(apidoc, queryDoc, queryModel, queryResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }

    s"generate proper Reactor for $createDoc" in
      providerTest(apidoc, createDoc, createModel, createResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }

    s"generate proper Reactor for $updateNumDoc" in
      providerTest(apidoc, updateNumDoc, updateNumModel, updateNumResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }

    s"generate proper Reactor for $updateNamDoc" in
      providerTest(apidoc, updateNamDoc, updateNamModel, updateNamResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }

    s"generate proper Reactor for $deleteNumDoc" in
      providerTest(apidoc, deleteNumDoc, deleteNumModel, deleteNumResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }

    s"generate proper Reactor for $deleteNamDoc" in
      providerTest(apidoc, deleteNamDoc, deleteNamModel, deleteNamResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }

    s"generate proper Reactor for $entityIntroDoc" in
      providerTest(apidoc, entityIntroDoc, entityIntroModel, entityIntroResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Documentation for Entity type foo")
      }

    s"generate proper Reactor for $introductionDoc" in
      providerTest(apidoc, introductionDoc, introductionModel, introductionResp) { (reactor: Reactor, response: Response) ⇒
        reactor.description must contain("Entity API Introduction")
      }

  }

}
