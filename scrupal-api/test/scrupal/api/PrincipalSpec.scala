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

import java.util.concurrent.TimeUnit

import scrupal.test.ScrupalSpecification
import scrupal.utils.HasherKinds

import scala.concurrent.Await
import scala.concurrent.duration.Duration


/**
 * Test that our basic abstractions for accessing the database hold water.
 */

class PrincipalSpec extends ScrupalSpecification("PrincipalSpec")
{
  sequential

	"Principal" should {
		"save, load and delete from DB" in {
      withSchema { schema: Schema =>
        withEmptyDB(schema.dbName) { database ⇒
          val p = new Principal('id, "nobody@nowhere.ex", List("Nobody"), "openpw",
            HasherKinds.SCrypt.toString, "", 0L, None)
          p._id must beEqualTo('id)
          val f1 = schema.principals.insert(p).flatMap { writeResult ⇒
            writeResult.ok must beTrue
            schema.principals.fetch('id) map {
              case Some(principal) ⇒
                principal._id must beEqualTo('id)
                principal.aliases must beEqualTo(List("Nobody"))
                principal.password must beEqualTo(p.password)
                schema.principals.removeById(principal._id) map { writeResult ⇒
                  writeResult.ok must beTrue
                }
                true
              case None ⇒ failure("No principal fetched"); false
            }
          }
          val r = Await.result(f1, Duration(1,TimeUnit.SECONDS))
          r must beTrue
        }
      }
    }
  }
}
