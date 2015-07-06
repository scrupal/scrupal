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

import scrupal.test.ScrupalSpecification

class ReverseAssetsSpec extends ScrupalSpecification("ReverseAssets") {

  lazy val ra = new ReverseAssets("")

  s"$specName" should {
    "generate stylesheet Call" in {
      val call = ra.css("scrupal.css")
      call.url must contain("/assets/stylesheets/scrupal.css")
    }
    "generate javascript Call" in {
      val call = ra.js("foo.js")
      call.url must contain("/assets/javascripts/foo.js")
    }
    "generate theme Call" in {
      val call = ra.theme("default")
      call.url must contain("/assets/theme/default")
    }
  }
}
