/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
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

package scrupal.core

import scrupal.test.ScrupalSpecification


/** One line sentence description here.
  * Further description here.
  */
class CoreSchemaSpec extends ScrupalSpecification("CoreSchemaSpec") {

  "CoreSchema" should {
    "Accumulate table names correctly" in {
      withCoreSchema { schema =>
        val names = schema.collectionNames
        names.contains("features") must beTrue
        names.contains("instances") must beTrue
        names.contains("alerts") must beTrue
        names.contains("sites") must beTrue
        names.contains("nodes") must beTrue
        names.contains("principals") must beTrue
      }
      success
    }
  }
}
