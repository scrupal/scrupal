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

import play.api.libs.json.Json
import scrupal.fakes.{WithFakeScrupal}
import scrupal.api._
import play.api.libs.json.JsObject
import scrupal.api.{Instance}
import scala.collection.immutable.HashMap
import scrupal.utils.Version

/**
 * One line sentence description here.
 * Further description here.
 */
class CoreSpec extends Specification {

  "Module Type, Entity and Instance " should {
    "support CRUD" in new WithFakeScrupal {
      withCoreSchema( { schema : CoreSchema =>
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

        val bun_id = Types.insert( new BundleType('Buns, "Buns Aye", m_id, HashMap('tie -> Type('Thai).get)))
        val bun = Types.fetch(bun_id)
        val bun2 = bun.get
        bun2.id must beEqualTo('Buns)
        bun_id must beEqualTo(bun2.id)

        val e_id = Entities.insert( new Entity('Plun, "Plunderous Thundering Fun", 'Buns ) {})
        val clob : JsObject = Json.obj("type" -> "Thai" )
        val instance = Instances.insert(Instance('Inst, "Instigating Instance", 'Plun, clob))
        val i2 = Instances.fetch(instance).get
        instance must beEqualTo(i2.id.get)
        i2.name must beEqualTo('Inst)
        i2.entityId must beEqualTo('Plun)

        // FIXME: Do Update and Delete too!
      })
  } }
}
