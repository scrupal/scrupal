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

import play.api.Environment
import play.api.test.FakeRequest
import scrupal.test.{OneAppPerSpec, ScrupalSpecification}
import scrupal.core.http.ErrorHandler
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class AssetsSpec extends ScrupalSpecification("Assets")  {

  lazy val eh = testScrupal.withConfiguration { config ⇒
    new ErrorHandler(testScrupal, Environment.simple(), config)
  }

  lazy val assets = new Assets(eh)

  s"$specName" should {
    "find a regular asset with at" in {
      val action = assets.at("stylesheets/scrupal.min.css")
      val req = FakeRequest("GET", "/assets/stylesheets/scrupal.min.css")
      val future = action.apply(req).map { result ⇒
        pending("getting application into test")
        // result.header.status must beEqualTo(200)
      }
      Await.result(future, 2.seconds)
    }
    // TODO: Write Asset test cases
    "find a javascript with js" in {
      pending
      // def js(file: String) = super.versioned("/public/javascripts", file)
    }
    "find an image with img" in {
      pending
      // def img(file: String) = super.versioned("/public/images", file)
    }
    "find a stylesheet with css" in {
      pending
      // def css(file: String) = super.versioned("/public/stylesheets", file)
    }
    "find a theme with theme" in {
      pending
      // def theme(theme: String, file: String) = super.versioned("/public/lib", s"bootswatch-$theme/$file")
    }
  }

}

