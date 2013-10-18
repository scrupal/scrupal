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

package scrupal.controllers

import org.specs2.mutable.Specification
import play.api.mvc.{Request, Headers, RequestHeader}
import scrupal.fakes.WithScrupal

/** This is the test suite for the Config.Step class
  *
  */
class ConfigStepSpec extends Specification {

  "Config.Step" should {
    "Identify Step 1" in new WithScrupal {
      implicit val context = Context()(new RequestHeader() {
        def headers: play.api.mvc.Headers = ???
        def id: Long = ???
        def method: String = ???
        def path: String = ???
        def queryString: Map[String,Seq[String]] = ???
        def remoteAddress: String = ???
        def tags: Map[String,String] = ???
        def uri: String = ???
        def version: String = ???
      } )
      val step = Config.Step(context)
      step must beEqualTo(Config.Step.One_Specify_Databases)
    }
  }
}
