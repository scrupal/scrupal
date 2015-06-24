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

package scrupal.utils

import org.specs2.mutable.Specification

/** One line sentence description here.
  * Further description here.
  */
class IconsSpec extends Specification {
  "icons" should {
    "return correct html for Icons.heart" in {
      Icons.html(Icons.heart).toString must beEqualTo("<i class=\"icon-heart\"></i>")
    }
    "return correct html for Icons.long_arrow_left" in {
      Icons.html(Icons.long_arrow_left).toString must beEqualTo("<i class=\"icon-long-arrow-left\"></i>")
    }
  }
}
