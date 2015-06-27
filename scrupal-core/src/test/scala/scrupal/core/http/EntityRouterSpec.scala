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
import scrupal.core.controllers.EntityRouter
import scrupal.test.{FakeContext, FakeEntity, ScrupalApiSpecification}

class EntityRouterSpec extends ScrupalApiSpecification("EntityRouter") {

  case class Fixture(name: String) extends {
    implicit val scrupal : Scrupal = testScrupal
  } with FakeContext[Fixture] {
    val fakeEntity = FakeEntity(name + "Entity", BundleType.empty)
    val router = EntityRouter(name, site.get, fakeEntity)
  }

  val pattern = Fixture("pattern")
  val singular = Fixture("singular")
  val plural = Fixture("plural")
  val combined = Fixture("combined")

  s"$specName routesPattern" should {
    "be defined for GET /23" in pattern { fix ⇒
      val req = FakeRequest(GET, "/23")
      fix.router.routesPattern.isDefinedAt(req) must beTrue
    }
    "be defined for GET /23/facet/facet _id" in pattern { fix ⇒
      val req = FakeRequest(GET, "/23/facet/facet_id")
      fix.router.routesPattern.isDefinedAt(req) must beTrue
    }
    "be defined for GET /23/facet/facet_id/rest-of-it" in pattern { fix ⇒
      val req = FakeRequest(GET, "/23/facet/facet_id/rest-of-it")
      fix.router.routesPattern.isDefinedAt(req) must beTrue
    }
  }

  s"$specName singular" should {
    "be defined for GET /singular/23" in singular { fix ⇒
      val req = FakeRequest(GET, "/singular/23")
      fix.router.singularRoutes.isDefinedAt(req) must beTrue
    }
  }

  s"$specName plural" should {
    "be defined for GET /plurals/23" in plural { fix ⇒
      val req = FakeRequest(GET, "/plurals/23")
      fix.router.pluralRoutes.isDefinedAt(req) must beTrue
    }
  }

  s"$specName combined" should {
    "be defined for both GET /combined/23 and GET /combineds/23" in combined { fix ⇒
      val req1 = FakeRequest(GET, "/combined/23")
      fix.router.combinedRoutes.isDefinedAt(req1) must beTrue
      val req2 = FakeRequest(GET, "/combineds/23")
      fix.router.combinedRoutes.isDefinedAt(req2) must beTrue
    }
  }

  // TODO: Implement the full set of test cases for EntityRouter
}
