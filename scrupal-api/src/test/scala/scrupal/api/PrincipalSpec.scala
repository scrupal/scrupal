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

import org.specs2.execute.AsResult
import scrupal.storage.api.{Collection, StoreContext, Schema}
import scrupal.test.ScrupalApiSpecification
import scrupal.utils.HasherKinds

import scala.concurrent.{ExecutionContext, Await}
import scala.concurrent.duration.Duration
import scala.concurrent.Future


/**
 * Test that our basic abstractions for accessing the database hold water.
 */

class PrincipalSpec extends ScrupalApiSpecification("Principal")
{
	"Principal" should {
		"save, load and delete from DB" in {
      withStoreContext { sc: StoreContext =>
        val p = new Principal('id, "nobody@nowhere.ex", List("Nobody"), "openpw",
          HasherKinds.SCrypt.toString, "", 0L, None)
        p._id must beEqualTo('id)
        implicit val ec : ExecutionContext = sc.ec
        val f = sc.addSchema(ApiSchemaDesign()).flatMap { schema : Schema ⇒
          schema.withCollection("principals") { principals : Collection[Principal] ⇒
            principals.insert(p).flatMap { writeResult ⇒
              writeResult.isSuccess must beTrue
              principals.fetch(p.getPrimaryId()) flatMap {
                case Some(principal) ⇒
                  principal.getPrimaryId() must beEqualTo(p.getPrimaryId())
                  principal._id must beEqualTo('id)
                  principal.aliases must beEqualTo(List("Nobody"))
                  principal.password must beEqualTo(p.password)
                  principals.delete(principal.getPrimaryId()) map { writeResult ⇒
                    writeResult.isSuccess must beTrue
                    success
                  }
                case None ⇒
                  Future.successful { failure("No principal fetched") }
              }
            }
          }
        }
        Await.result(f, Duration(1,TimeUnit.SECONDS))
      }
    }
  }
}
