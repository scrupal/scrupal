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

package scrupal.models

import org.specs2.mutable.Specification

import scrupal.test.{WithDBSession}
import scrupal.models.db.{Bundle, Type, Module}
import play.api.libs.json.{Json, JsNumber, JsObject}
import java.sql.Clob

/**
 * One line sentence description here.
 * Further description here.
 */
class CoreSpec extends Specification {

  "Module, Type, Bundle" should {
    "save to and fetch from the DB" in new WithDBSession {
      import schema._
      val mod = Modules.insert(Module('foo, "Foo Man Chew", 1, 0))
      mod.name must beEqualTo('foo)
      val mod2 = Modules.fetch(mod.id.get).get
      mod.id must beEqualTo(mod2.id)
      val ty = Types.insert(Type('Thai, "Thai Foon", mod.id.get ))
      ty.name must beEqualTo('Thai)
      val ty2 = Types.fetch(ty.id.get).get
      ty.id must beEqualTo(ty2.id)
      ty2.moduleId must beEqualTo( mod2.id.get )
      val clob : Clob = schema.toClob(Json.obj("type" -> JsNumber(ty.id.get)))
      val bundle = Bundles.insert(Bundle('Buns, "Buns Aye", ty.id.get, clob))
      bundle.name must beEqualTo('Buns)
      val bun2 = Bundles.fetch(bundle.id.get).get
      bundle.id must beEqualTo(bun2.id)
      bun2.typeId must beEqualTo(ty2.id.get)
    }
  }
}
