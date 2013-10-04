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

package models.db

import play.api.test.Helpers.running

import org.specs2.mutable.Specification
import org.joda.time.DateTime
import scrupal.models.db._
import scrupal.models.db.Principal
import scrupal.test.FakeScrupal
import scala.slick.session.Session
import scrupal.utils.HasherKinds
import akka.actor.IO.Handle


/**
 * Test that our basic abstractions for accessing the database hold water.
 */

class UserSpec extends Specification
{
	"Principal" should {
		"save, load and delete from DB" in {
      running(FakeScrupal) {
        FakeScrupal.db withSession { implicit session : Session =>
          val sk : Sketch = FakeScrupal.sketch
          val ts = new ScrupalSchema(FakeScrupal.sketch)
          ts.create
          val p = new Principal(None, DateTime.now(), "nobody@nowhere.ex", "openpw",  HasherKinds.SCrypt.toString() )
          val p2 = ts.Principals.upsert(p)
          val p3 = ts.Principals.fetch(p2.id.get)
          p3.isDefined must beTrue
          val p4 = p3.get
          p4.id.equals(p2.id) must beTrue
          p4.password.equals(p4.password) must beTrue
          ts.Principals.delete(p4.id) must beTrue
        }
        success
      }
    }
  }

  "Handle" should {
    "save, load and delete from DB" in {
      running(FakeScrupal) {
        FakeScrupal.db withSession { implicit session : Session =>
          val sk : Sketch = FakeScrupal.sketch
          val ts = new ScrupalSchema(FakeScrupal.sketch)
          ts.create
          val p = new Principal(None, DateTime.now(), "nobody@nowhere.ex", "openpw",  HasherKinds.SCrypt.toString() )
          val p2 : Principal = ts.Principals.upsert(p)
          ts.Handles.insert("nobody", p2.id.get)
          val p3 : Principal = ts.Handles.principals("nobody").head
          p3.id must beEqualTo(p2.id)
          val h : String = ts.Handles.handles(p2.id.get).head
          h must beEqualTo("nobody")
          ts.Handles.delete("nobody") must beTrue
        }
      }
    }
    "allow many-to-many relations with Principal" in {
      success
    }
  }
}
