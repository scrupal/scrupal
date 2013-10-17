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

import play.api.libs.json.Json
import scrupal.fakes.{WithScrupal}
import scrupal.api._
import play.api.libs.json.JsObject
import scrupal.api.{Entity}
import scala.slick.lifted.Query
import scrupal.models.db.ScrupalSchema

/**
 * One line sentence description here.
 * Further description here.
 */
class CoreSpec extends Specification {

  "Module Type and Entity " should {
    "support CRud" in new WithScrupal {
      withScrupalSchema( { schema : ScrupalSchema =>
        import schema._
        val m_id = Modules.insert ( EssentialModule('foo, "Foo Man Chew", Version(0,1,0), Version(0,0,0), true) )
        m_id must beEqualTo('foo)
        println("Module ID=" + m_id)

        val ty_id = Types.insert(new StringType('Thai, "Thai Foon", m_id, ".*".r ))
        val ty = Types.fetch(ty_id)
        val ty2 = ty.get
        ty2.id must beEqualTo('Thai)
        ty_id must beEqualTo(ty2.id)
        ty2.moduleId must beEqualTo( 'foo )

        val clob : JsObject = Json.obj("type" -> "Thai" )
        val entity = Entities.insert(Entity('Buns, "Buns Aye", 'Thai, clob))
        val bun2 = Entities.fetch(entity).get
        entity must beEqualTo(bun2.id.get)
        bun2.name must beEqualTo('Buns)
        bun2.entityTypeId must beEqualTo('Thai)
      })
  } }
}
