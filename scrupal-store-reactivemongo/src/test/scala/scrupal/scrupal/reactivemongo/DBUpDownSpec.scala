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

package scrupal.store.reactivemongo

import org.specs2.mutable.Specification

/**
 * Created by reidspencer on 11/9/14.
 */
class DBUpDownSpec extends Specification {

  "DBContext" should {
    "startup and shutdown only once" in {
      val n = DBContext.numberOfStartups
      DBContext.startup()
      DBContext.startup()
      DBContext.numberOfStartups must beEqualTo(n+2)
      DBContext.isStartedUp must beTrue
      DBContext.shutdown()
      DBContext.numberOfStartups must beEqualTo(n+1)
      DBContext.isStartedUp must beTrue
      DBContext.shutdown()
      DBContext.numberOfStartups must beEqualTo(n+0)
      DBContext.isStartedUp must beEqualTo(n != 0)
    }
  }
}
