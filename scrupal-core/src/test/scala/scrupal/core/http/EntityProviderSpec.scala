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

package scrupal.core.http

import play.api.test.FakeRequest
import scrupal.api.{Scrupal, BundleType}
import scrupal.test.{FakeContext, FakeEntity, ScrupalApiSpecification}

class EntityProviderSpec extends ScrupalApiSpecification("EntityRouter") {

  case class Fixture(name: String) extends {
    implicit val scrupal : Scrupal = testScrupal
  } with FakeContext[Fixture] {
    val fe = FakeEntity(name, BundleType.empty)
  }

  val fixture = Fixture("fix")

  s"$specName singular" should {
    "be defined for GET /23/facet/facet_id/rest-of-it" in fixture { fix ⇒
      val req = FakeRequest(GET, "/23/facet/facet_id/rest-of-it")
      fix.fe.singularRoutes.isDefinedAt(req) must beTrue
    }
  }

  s"$specName plural" should {
    "be defined for GET /23" in fixture { fix ⇒
      val req = FakeRequest(GET, "/23")
      fix.fe.pluralRoutes.isDefinedAt(req) must beTrue
    }
  }

  s"$specName combined" should {
    "be defined for both GET /fix/23/facet/id and GET /fixes/23" in fixture { fix ⇒
      val req1 = FakeRequest(GET, "/fix/23/facet/id")
      fix.fe.provide.isDefinedAt(req1) must beTrue
      val req2 = FakeRequest(GET, "/fixes/23")
      fix.fe.provide.isDefinedAt(req2) must beTrue
    }
    "be defined for GET /fix/23/facet/facet _id" in fixture { fix ⇒
      val req = FakeRequest(GET, "/fix/23/facet/facet_id")
      fix.fe.provide.isDefinedAt(req) must beTrue
    }
  }

  // TODO: Implement the full set of test cases for EntityRouter
}
