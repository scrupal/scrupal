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

package scrupal.core.entities

import play.api.test.FakeRequest
import scrupal.api._
import scrupal.test.{ScrupalApiSpecification}

/**
 * Created by reid on 11/11/14.
 */
class EchoEntitySpec extends ScrupalApiSpecification("EchoEntity") {

  lazy val echo = EchoEntity('EchoSpec)(scrupal)

  lazy val retrieveNum = FakeRequest("GET", "/echospecs/1/rest")
  lazy val retrieveNam = FakeRequest("GET", "/echospecs/name/rest")
  lazy val infoNum = FakeRequest("HEAD", "/echospecs/1/rest")
  lazy val infoNam = FakeRequest("HEAD", "/echospecs/name/rest")
  lazy val query = FakeRequest("OPTIONS", "/echospecs/rest")
  lazy val create = FakeRequest("POST", "/echospecs/rest")
  lazy val updateNum = FakeRequest("PUT", "/echospecs/1/rest")
  lazy val updateNam = FakeRequest("PUT", "/echospecs/name/rest")
  lazy val deleteNum = FakeRequest("DELETE", "/echospecs/1/rest")
  lazy val deleteNam = FakeRequest("DELETE", "/echospecs/name/rest")
  lazy val getNum = FakeRequest("GET", "/echospec/1/facet/facet_id/rest")
  lazy val getNam = FakeRequest("GET", "/echospec/name/facet/facet_id/rest")
  lazy val facetInfoNum = FakeRequest("HEAD", "/echospec/1/facet/facet_id/rest")
  lazy val facetInfoNam = FakeRequest("HEAD", "/echospec/name/facet/facet_id/rest")
  lazy val findNum = FakeRequest("OPTIONS", "/echospec/1/facet/rest")
  lazy val findNam = FakeRequest("OPTIONS", "/echospec/name/facet/rest")
  lazy val addNum = FakeRequest("POST", "/echospec/1/facet/rest")
  lazy val addNam = FakeRequest("POST", "/echospec/name/facet/rest")
  lazy val setNum = FakeRequest("PUT", "/echospec/1/facet/facet_id/rest")
  lazy val setNam = FakeRequest("PUT", "/echospec/name/facet/facet_id/rest")
  lazy val removeNum = FakeRequest("DELETE", "/echospec/1/facet/facet_id/rest")
  lazy val removeNam = FakeRequest("DELETE", "/echospec/name/facet/facet_id/rest")

  "EchoEntity" should {
    s"generate proper Reactor for $retrieveNum" in {
      val reactorOption = echo.provide.lift.apply(retrieveNum)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoRetrieve")
      reactor.isInstanceOf[EntityRetrieve] must beTrue
    }
    s"generate proper Reactor for $retrieveNam" in {
      val reactorOption = echo.provide.lift.apply(retrieveNam)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoRetrieve")
      reactor.isInstanceOf[EntityRetrieve] must beTrue
    }
    s"generate proper Reactor for $infoNum" in  {
      val reactorOption = echo.provide.lift.apply(infoNum)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoInfo")
      reactor.isInstanceOf[EntityInfo] must beTrue
    }
    s"generate proper Reactor for $infoNam" in {
      val reactorOption = echo.provide.lift.apply(infoNam)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoInfo")
      reactor.isInstanceOf[EntityInfo] must beTrue
    }
    s"generate proper Reactor for $query" in  {
      val reactorOption = echo.provide.lift.apply(query)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoQuery")
      reactor.isInstanceOf[EntityQuery] must beTrue
    }
    s"generate proper Reactor for $create" in {
      val reactorOption = echo.provide.lift.apply(create)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoCreate")
      reactor.isInstanceOf[EntityCreate] must beTrue
    }
    s"generate proper Reactor for $updateNum" in  {
      val reactorOption = echo.provide.lift.apply(updateNum)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoUpdate")
      reactor.isInstanceOf[EntityUpdate] must beTrue
    }
    s"generate proper Reactor for $updateNam" in {
      val reactorOption = echo.provide.lift.apply(updateNam)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoUpdate")
      reactor.isInstanceOf[EntityUpdate] must beTrue
    }
    s"generate proper Reactor for $deleteNum" in  {
      val reactorOption = echo.provide.lift.apply(deleteNum)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoDelete")
      reactor.isInstanceOf[EntityDelete] must beTrue
    }
    s"generate proper Reactor for $deleteNam" in {
      val reactorOption = echo.provide.lift.apply(deleteNam)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoDelete")
      reactor.isInstanceOf[EntityDelete] must beTrue
    }
    s"generate proper Reactor for $getNum" in  {
      val reactorOption = echo.provide.lift.apply(getNum)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoGet")
      reactor.isInstanceOf[EntityGet] must beTrue
    }
    s"generate proper Reactor for $getNam" in {
      val reactorOption = echo.provide.lift.apply(getNam)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoGet")
      reactor.isInstanceOf[EntityGet] must beTrue
    }

    s"generate proper Reactor for $facetInfoNum" in  {
      val reactorOption = echo.provide.lift.apply(facetInfoNum)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoFacetInfo")
      reactor.isInstanceOf[EntityFacetInfo] must beTrue
    }
    s"generate proper Reactor for $facetInfoNam" in {
      val reactorOption = echo.provide.lift.apply(facetInfoNam)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoFacetInfo")
      reactor.isInstanceOf[EntityFacetInfo] must beTrue
    }

    s"generate proper Reactor for $findNum" in  {
      val reactorOption = echo.provide.lift.apply(findNum)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoFind")
      reactor.isInstanceOf[EntityFind] must beTrue
    }
    s"generate proper Reactor for $findNam" in {
      val reactorOption = echo.provide.lift.apply(findNam)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoFind")
      reactor.isInstanceOf[EntityFind] must beTrue
    }

    s"generate proper Reactor for $addNum" in  {
      val reactorOption = echo.provide.lift.apply(addNum)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoAdd")
      reactor.isInstanceOf[EntityAdd] must beTrue
    }
    s"generate proper Reactor for $addNam" in {
      val reactorOption = echo.provide.lift.apply(addNam)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoAdd")
      reactor.isInstanceOf[EntityAdd] must beTrue
    }

    s"generate proper Reactor for $setNum" in  {
      val reactorOption = echo.provide.lift.apply(setNum)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoSet")
      reactor.isInstanceOf[EntitySet] must beTrue
    }
    s"generate proper Reactor for $setNam" in {
      val reactorOption = echo.provide.lift.apply(setNam)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoSet")
      reactor.isInstanceOf[EntitySet] must beTrue
    }

    s"generate proper Reactor for $removeNum" in  {
      val reactorOption = echo.provide.lift.apply(removeNum)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoRemove")
      reactor.isInstanceOf[EntityRemove] must beTrue
    }
    s"generate proper Reactor for $removeNam" in {
      val reactorOption = echo.provide.lift.apply(removeNam)
      reactorOption.isDefined must beTrue
      val reactor = reactorOption.get
      reactor.name must beEqualTo("EchoRemove")
      reactor.isInstanceOf[EntityRemove] must beTrue
    }
  }
}
