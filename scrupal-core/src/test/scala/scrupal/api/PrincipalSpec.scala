/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Web Application Framework for Content Management                 *
 *                                                                                                                    *
 * Copyright (c) 2015, Reactific Software LLC. All Rights Reserved.                                                   *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     *
 * with the License. You may obtain a copy of the License at                                                          *
 *                                                                                                                    *
 *     http://www.apache.org/licenses/LICENSE-2.0                                                                     *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   *
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  *
 * the specific language governing permissions and limitations under the License.                                     *
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
