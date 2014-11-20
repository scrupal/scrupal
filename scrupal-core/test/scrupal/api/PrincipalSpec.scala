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

package scrupal.api

import scrupal.core.CoreSchema
import scrupal.test.ScrupalSpecification
import scrupal.utils.HasherKinds


/**
 * Test that our basic abstractions for accessing the database hold water.
 */

class PrincipalSpec extends ScrupalSpecification("PrincipalSpec")
{
  sequential

	"Principal" should {
		"save, load and delete from DB" in {
      withSchema { schema: Schema =>
        val p = new Principal('id, "nobody@nowhere.ex", "openpw",  HasherKinds.SCrypt.toString(), "", 0L, None)
        p._id must beEqualTo('id)
/* FIXME:       val p2 : Identifier = Principals.insert(p)
        val p3 = Principals.fetch(p2)
        p3.isDefined must beTrue
        val p4 = p3.get
        p4.id.get must beEqualTo (p2)
        p4.password.equals(p4.password) must beTrue
        Principals.delete(p4) must beTrue
        */
      }
    }
  }

  "Handle" should {
    "save, load and delete from DB" in {
      withSchema { schema: Schema =>
        val p = new Principal('id, "nobody@nowhere.ex", "openpw",  HasherKinds.SCrypt.toString(), "", 0L, None )
        p._id must beEqualTo('id)

/* FIXME:       val p2 : Identifier = principals.upsert(p)
        Handles.insert("nobody", p2)
        val p3 : Principal = Handles.principals("nobody").head
        p3.id.isDefined must beTrue
        p3.id.get must beEqualTo(p2)
        val h : String = Handles.handles(p2).head
        h must beEqualTo("nobody")
        Handles.delete("nobody") must beTrue
  */    }
    }

    "allow many-to-many relations with Principal" in {
      withSchema { schema: Schema =>
/* FIXME:        import schema._
        val p1 = new Principal( "nobody@nowhere.ex", "openpw", HasherKinds.SCrypt.toString() )
        val p2 = new Principal("exempli@gratis.org", "openpw", HasherKinds.SCrypt.toString() )
        val p1id = Principals.upsert(p1);
        val p2id = Principals.insert(p2);
        Handles.insert("nobody", p1id)
        Handles.insert("nowhere", p1id)
        Handles.insert("exempli", p2id)
        Handles.insert("gratis", p2id)
        Handles.insert("nobody", p2id)
        Handles.insert("nowhere", p2id)
        Handles.insert("exempli", p1id)
        Handles.insert("gratis", p1id)

        Handles.principals("nobody").count(p => true) must beEqualTo(2)
        Handles.principals("nowhere").count(p => true) must beEqualTo(2)
        Handles.principals("exempli").count(p => true) must beEqualTo(2)
        Handles.principals("gratis").count(p => true) must beEqualTo(2)
        Handles.handles(p1id).count(p => true) must beEqualTo(4)
        Handles.handles(p2id).count(p => true) must beEqualTo(4)
        Handles.handles(p1id).count(p => p.contains("n")) must beEqualTo(2)
        Handles.handles(p2id).count(p => p.contains("e")) must beEqualTo(2)
        */
        success
      }
    }
  }
}
