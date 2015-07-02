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
import scrupal.api.Scrupal
import scrupal.test.{FakeContext, ScrupalApiSpecification}

class APIDocSpec extends ScrupalApiSpecification("APIDoc") {

  lazy val getNumDoc = FakeRequest("GET", "/apidoc/foo/GET/1/facet/facet_id/rest")
  lazy val getNamDoc = FakeRequest("GET", "/apidoc/foo/GET/name/facet/facet_id/rest")
  lazy val facetInfoNumDoc = FakeRequest("GET", "/apidoc/foo/HEAD/1/facet/facet_id/rest")
  lazy val facetInfoNamDoc = FakeRequest("GET", "/apidoc/foo/HEAD/name/facet/facet_id/rest")
  lazy val findNumDoc = FakeRequest("GET", "/apidoc/foo/OPTIONS/1/facet/rest")
  lazy val findNamDoc = FakeRequest("GET", "/apidoc/foo/OPTIONS/name/facet/rest")
  lazy val addNumDoc = FakeRequest("GET", "/apidoc/foo/POST/1/facet/rest")
  lazy val addNamDoc = FakeRequest("GET", "/apidoc/foo/POST/name/facet/rest")
  lazy val setNumDoc = FakeRequest("GET", "/apidoc/foo/PUT/1/facet/facet_id/rest")
  lazy val setNamDoc = FakeRequest("GET", "/apidoc/foo/PUT/name/facet/facet_id/rest")
  lazy val removeNumDoc = FakeRequest("GET", "/apidoc/foo/DELETE/1/facet/facet_id/rest")
  lazy val removeNamDoc = FakeRequest("GET", "/apidoc/foo/DELETE/name/facet/facet_id/rest")
  lazy val retrieveNumDoc= FakeRequest("GET", "/apidoc/foo/GET/1/rest")
  lazy val retrieveNamDoc = FakeRequest("GET", "/apidoc/foo/GET/name/rest")
  lazy val infoNumDoc= FakeRequest("GET", "/apidoc/foo/HEAD/1/rest")
  lazy val infoNamDoc = FakeRequest("GET", "/apidoc/foo/HEAD/name/rest")
  lazy val queryDoc = FakeRequest("GET", "/apidoc/foo/OPTIONS/rest")
  lazy val createDoc = FakeRequest("GET", "/apidoc/foo/POST/rest")
  lazy val updateNumDoc = FakeRequest("GET", "/apidoc/foo/PUT/1/rest")
  lazy val updateNamDoc = FakeRequest("GET", "/apidoc/foo/PUT/name/rest")
  lazy val deleteNumDoc = FakeRequest("GET", "/apidoc/foo/DELETE/1/rest")
  lazy val deleteNamDoc = FakeRequest("GET", "/apidoc/foo/DELETE/name/rest")
  lazy val entityIntroDoc = FakeRequest("GET", "/apidoc/foo")
  lazy val introductionDoc = FakeRequest("GET", "/apidoc")

  case class fixture() extends {
    val name = "APIDocFixture"
    implicit val scrupal: Scrupal = testScrupal
  } with FakeContext[fixture] {
    val apidoc = APIDoc()
  }

  "APIDoc" should {
    s"generate proper Reactor for $getNumDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(getNumDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("GetNumDoc")
      reactor must beEqualTo(fix.apidoc.GetNumDoc("foo", 1, "facet", "facet_id", "/rest"))
      // TODO: test result returned by Reactor
    }
    s"generate proper Reactor for $getNamDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(getNamDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("GetNameDoc")
      reactor must beEqualTo(fix.apidoc.GetNameDoc("foo", "name", "facet", "facet_id", "/rest"))
      // TODO: test result returned by Reactor
    }
    s"generate proper Reactor for $facetInfoNumDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(facetInfoNumDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("FacetInfoNumDoc")
      reactor must beEqualTo(fix.apidoc.FacetInfoNumDoc("foo", 1, "facet", "facet_id", "/rest"))
      // TODO: test result returned by Reactor
    }
    s"generate proper Reactor for $facetInfoNamDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(facetInfoNamDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("FacetInfoNameDoc")
      reactor must beEqualTo(fix.apidoc.FacetInfoNameDoc("foo", "name", "facet", "facet_id", "/rest"))
      // TODO: test result returned by Reactor
    }

    s"generate proper Reactor for $findNumDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(findNumDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("FindNumDoc")
      reactor must beEqualTo(fix.apidoc.FindNumDoc("foo", 1, "facet", "/rest"))
      // TODO: test result returned by Reactor
    }

    s"generate proper Reactor for $findNamDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(findNamDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("FindNameDoc")
      reactor must beEqualTo(fix.apidoc.FindNameDoc("foo", "name", "facet", "/rest"))
      // TODO: test result returned by Reactor
    }

    s"generate proper Reactor for $addNumDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(addNumDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("AddNumDoc")
      reactor must beEqualTo(fix.apidoc.AddNumDoc("foo", 1, "facet", "/rest"))
      // TODO: test result returned by Reactor
    }

    s"generate proper Reactor for $addNamDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(addNamDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("AddNameDoc")
      reactor must beEqualTo(fix.apidoc.AddNameDoc("foo", "name", "facet", "/rest"))
      // TODO: test result returned by Reactor
    }

    s"generate proper Reactor for $setNumDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(setNumDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("SetNumDoc")
      reactor must beEqualTo(fix.apidoc.SetNumDoc("foo", 1, "facet", "facet_id", "/rest"))
      // TODO: test result returned by Reactor
    }

    s"generate proper Reactor for $setNamDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(setNamDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("SetNameDoc")
      reactor must beEqualTo(fix.apidoc.SetNameDoc("foo", "name", "facet", "facet_id", "/rest"))
      // TODO: test result returned by Reactor
    }
    s"generate proper Reactor for $removeNumDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(removeNumDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("RemoveNumDoc")
      reactor must beEqualTo(fix.apidoc.RemoveNumDoc("foo", 1, "facet", "facet_id", "/rest"))
      // TODO: test result returned by Reactor
    }

    s"generate proper Reactor for $removeNamDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(removeNamDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("RemoveNameDoc")
      reactor must beEqualTo(fix.apidoc.RemoveNameDoc("foo", "name", "facet", "facet_id", "/rest"))
      // TODO: test result returned by Reactor
    }

    s"generate proper Reactor for $retrieveNumDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(retrieveNumDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("RetrieveNumDoc")
      reactor must beEqualTo(fix.apidoc.RetrieveNumDoc("foo", 1, "/rest"))
      // TODO: test result returned by Reactor
    }

    s"generate proper Reactor for $retrieveNamDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(retrieveNamDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("RetrieveNameDoc")
      reactor must beEqualTo(fix.apidoc.RetrieveNameDoc("foo", "name", "/rest"))
      // TODO: test result returned by Reactor
    }

    s"generate proper Reactor for $infoNumDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(infoNumDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("InfoNumDoc")
      reactor must beEqualTo(fix.apidoc.InfoNumDoc("foo", 1, "/rest"))
      // TODO: test result returned by Reactor
    }

    s"generate proper Reactor for $infoNamDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(infoNamDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("InfoNameDoc")
      reactor must beEqualTo(fix.apidoc.InfoNameDoc("foo", "name", "/rest"))
      // TODO: test result returned by Reactor
    }

    s"generate proper Reactor for $queryDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(queryDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("QueryDoc")
      reactor must beEqualTo(fix.apidoc.QueryDoc("foo", "/rest"))
      // TODO: test result returned by Reactor
    }

    s"generate proper Reactor for $createDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(createDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("CreateDoc")
      reactor must beEqualTo(fix.apidoc.CreateDoc("foo", "/rest"))
      // TODO: test result returned by Reactor
    }

    s"generate proper Reactor for $updateNumDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(updateNumDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("UpdateNumDoc")
      reactor must beEqualTo(fix.apidoc.UpdateNumDoc("foo", 1, "/rest"))
      // TODO: test result returned by Reactor
    }

    s"generate proper Reactor for $updateNamDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(updateNamDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("UpdateNameDoc")
      reactor must beEqualTo(fix.apidoc.UpdateNameDoc("foo", "name", "/rest"))
      // TODO: test result returned by Reactor
    }

    s"generate proper Reactor for $deleteNumDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(deleteNumDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("DeleteNumDoc")
      reactor must beEqualTo(fix.apidoc.DeleteNumDoc("foo", 1, "/rest"))
      // TODO: test result returned by Reactor
    }

    s"generate proper Reactor for $deleteNamDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(deleteNamDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("DeleteNameDoc")
      reactor must beEqualTo(fix.apidoc.DeleteNameDoc("foo", "name", "/rest"))
      // TODO: test result returned by Reactor
    }

    s"generate proper Reactor for $entityIntroDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(entityIntroDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EntityIntroduction")
      reactor must beEqualTo(fix.apidoc.EntityIntroduction("foo"))
      // TODO: test result returned by Reactor
    }

    s"generate proper Reactor for $introductionDoc" in fixture() { fix : fixture ⇒
      val reactorOption = fix.apidoc.provide.lift.apply(introductionDoc)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("ApiDocIntroduction")
      reactor must beEqualTo(fix.apidoc.ApiDocIntroduction())
      // TODO: test result returned by Reactor
    }


    /*
        lazy val entityIntroDoc = FakeRequest("GET", "/apidoc/foo")
        lazy val introductionDoc = FakeRequest("GET", "")
    */
  }

}
