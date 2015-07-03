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

package scrupal.api

import org.specs2.mutable.Specification
import scrupal.api.html.Icons

/** Test Cases For Icons */
class IconsSpec extends Specification {
  "icons" should {
    "return correct html for Icons.heart" in {
      Icons.heart.toString must beEqualTo("<i class=\"icon-heart\"></i>")
    }
    "return correct html for Icons.long_arrow_left" in {
      Icons.long_arrow_left.toString must beEqualTo("<i class=\"icon-long-arrow-left\"></i>")
    }
    "return correct html for Icons.ok" in {
      Icons.ok.toString must beEqualTo("<i class=\"icon-ok\"></i>")
    }

    "return correct html for Icons.info" in {
      Icons.info.toString must beEqualTo("<i class=\"icon-info\"></i>")
    }

    "return correct html for Icons.exclamation" in {
      Icons.exclamation.toString must beEqualTo("<i class=\"icon-exclamation\"></i>")
    }

    "return correct html for Icons.exclamation_sign" in {
      Icons.exclamation_sign.toString must beEqualTo("<i class=\"icon-exclamation-sign\"></i>")
    }

    "return correct html for Icons.remove" in {
      Icons.remove.toString must beEqualTo("<i class=\"icon-remove\"></i>")
    }

    "return correct html for Icons.warning_sign" in {
      Icons.warning_sign.toString must beEqualTo("<i class=\"icon-warning-sign\"></i>")
    }
  }
}
