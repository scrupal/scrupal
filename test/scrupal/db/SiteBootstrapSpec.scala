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

package scrupal.db

import org.specs2.mutable.Specification

/** Test cases for the SiteBootstrap class.
  */
class SiteBootstrapSpec extends Specification {

  val testLineNoNL = "foo\tjdbc:h2:mem:"

  "SiteBootstrap" should {
    "parse simple test line correctly" in {
      val sites : SiteBootstrap.Site2Jdbc = SiteBootstrap.get("foo\tjdbc:h2:mem:")
      sites.size must beEqualTo(1)
      val siteo = sites.get("foo")
      siteo.isDefined must beTrue
      val site = siteo.get
      site._1 must beEqualTo("jdbc:h2:mem:")
      site._2.isDefined must beFalse
    }
  }
}
