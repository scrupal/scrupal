/** ********************************************************************************************************************
  * Copyright © 2014 Reactific Software, Inc.                                                                          *
  *                                                                                                                 *
  * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
  *                                                                                                                 *
  * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
  * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
  * or (at your option) any later version.                                                                             *
  *                                                                                                                 *
  * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
  * details.                                                                                                           *
  *                                                                                                                 *
  * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
  * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
  * ********************************************************************************************************************
  */

package scrupal.utils

import org.specs2.mutable.Specification

/** One line sentence description here.
  * Further description here.
  */
class MathHelpersSpec extends Specification {

  "MathHelpers" should {
    "compute log2 sanely" in {
      val x : Long = 0x18000000000L // 2^40 + 2^39
      val log2_x = MathHelpers.log2(x)
      val y : Int = 0x18000 // 2^16 + 2^15
      val log2_y = MathHelpers.log2(y)
      log2_x must beEqualTo(40)
      log2_y must beEqualTo(16)
    }
  }

}
