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

package router.scrupal.core

import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject._
import play.api.Environment
import play.api.mvc.Handler
import play.api.routing.Router
import play.api.test.FakeRequest
import scrupal.api.DataCache
import scrupal.storage.api.Schema
import scrupal.test.{OneAppPerSpec, ScrupalSpecification}
import scrupal.core.http.ErrorHandler
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


class AssetsSpec extends ScrupalSpecification("Assets") with OneAppPerSpec {

  override   def fakeApplicationBuilder(
    path: java.io.File = new java.io.File("."),
    classloader: ClassLoader = classOf[OneAppPerSpec].getClassLoader,
    additionalConfiguration: Map[String, _ <: Any] = Map.empty,
    withRoutes: PartialFunction[(String, String), Handler] = PartialFunction.empty,
    router : Router = null
  ) : GuiceApplicationBuilder = {
    super.fakeApplicationBuilder(path,classloader,additionalConfiguration)
      .overrides(bind[Router].to[_root_.router.Routes])
  }

  lazy val eh = testScrupal.withConfiguration { config ⇒
    new ErrorHandler(testScrupal, Environment.simple(), config)
  }

  lazy val assets = new Assets(eh)

  s"$specName" should {
    "find a regular asset with at" in {
      val action = assets.at("stylesheets/scrupal.min.css")
      val req = FakeRequest("GET", "/assets/stylesheets/scrupal.min.css")
      val future = action.apply(req).map { result ⇒
        result.header.status must beEqualTo(200)
      }
      Await.result(future, 2.seconds)
    }
    "find an image with img" in {
      val action = assets.img("scrupal.ico")
      val req = FakeRequest("GET", "/assets/images/scrupal.ico")
      val future = action.apply(req).map { result ⇒
        result.header.status must beEqualTo(200)
      }
      Await.result(future, 2.seconds)
    }
    "find a stylesheet with css" in {
      val action = assets.css("scrupal.min.css")
      val req = FakeRequest("GET", "/assets/stylesheets/scrupal.min.css")
      val future = action.apply(req).map { result ⇒
        result.header.status must beEqualTo(200)
      }
      Await.result(future, 2.seconds)
    }
    "find a theme with theme" in {
      DataCache.updateThemeInfo
      val action = assets.theme("Cyborg")
      val req = FakeRequest("GET", "/assets/theme/cyborg")
      val future = action.apply(req).map { result ⇒
        result.header.status must beEqualTo(200)
      }
      Await.result(future, 2.seconds)
    }
    // TODO: Write an asset test for javascript files

  }

}

