/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2013, Reid Spencer and viritude llc. All Rights Reserved.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/

package scrupal.utils

import org.specs2.mutable.Specification

/**
 * One line sentence description here.
 * Further description here.
 */
class RegistrationSpec extends Specification {

  "Registration" should {
    "allow Registrable to be mixed in" in {
      abstract class Test_Registrable extends Registrable
      object one extends Test_Registrable {
        override val id = 'one
      }
      object two extends Test_Registrable {
        override val id = 'two
      }
      object registry extends Registry[Test_Registrable] {
        override val registryName = "Test-Registry"
        override val registrantsName = "Test-Registrable"
      }
      registry.register(one)
      registry.register(two)
      registry.size must beEqualTo(2)
      registry.unRegister(two)
      registry.size must beEqualTo(1)
      registry.unRegister(one)
      registry.size must beEqualTo(0)
    }
  }
}
