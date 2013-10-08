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

package scrupal.api

import org.specs2.mutable.Specification
import play.api.libs.json.{JsError, JsSuccess, JsString, Json}

/** Test that the Setting and SettingsGroup classes do their jobs correctly */
class SettingsSpec extends Specification {
  val fooType = StringType('TestType, "Foo", "^foo$".r, 32)

  "Settings" should {
    "validate simple foo pattern" in {
      val s = Setting('name, fooType, "Setting description")
      s.validate( JsString("foo")).get must beTrue
    }
  }

  "SettingsGroup" should {
    "allow simple construction" in {
      val nested = SettingsGroup('group, "Group description", Seq(
        Setting('name, fooType, "Setting description")
      ))

      val result = nested.validate(
        Json.obj( "group" -> Json.obj(
          "name" -> "foo")
        )
      ) match {
        case x: JsSuccess[Boolean] => x
        case y: JsError => { println(y); y }
      }
      result.getOrElse(false) must beTrue
    }
  }

}
