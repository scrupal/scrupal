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

  def expectValid(data: String, expected_site: String) = {
    val sites : SiteBootstrap.Site2Jdbc = SiteBootstrap.get(data)
    sites.size must beEqualTo(1)
    val siteo = sites.get(expected_site)
    siteo.isDefined must beTrue
    val site = siteo.get
    site._1.startsWith("jdbc:") must beTrue
    site._2.isDefined must beFalse
  }

  def expectInvalid(data: String, expected_site: String, error_contains: String) = {
    val sites : SiteBootstrap.Site2Jdbc = SiteBootstrap.get(data)
    sites.size must beEqualTo(1)
    val siteo = sites.get(expected_site)
    siteo.isDefined must beTrue
    val site = siteo.get
    site._2.isDefined must beTrue
    site._2.get.contains(error_contains)
  }

  def expectNothing(data:String) = {
    val sites : SiteBootstrap.Site2Jdbc = SiteBootstrap.get(data)
    sites.size must beEqualTo(0)
  }

  "SiteBootstrap" should {
    "parse simple test line correctly" in {
      expectValid("foo\tjdbc:h2:mem:", "foo")
    }

    "ignores leading white space" in {
      expectValid("  foo\tjdbc:h2:mem:", "foo")
    }

    "ignores trailing white space" in {
      expectValid("foo\tjdbc:h2:mem:  ", "foo")
    }

    "expects url to start with jdbc:" in {
      expectInvalid("foo\thttp://h2:mem:", "foo", "JDBC")
    }

    "return correct error for empty site part" in {
      expectInvalid(" \tjdbc:h2:mem:", "", "Wrong number")
    }

    "elide junk" in {
      expectNothing("")
    }
  }
}
