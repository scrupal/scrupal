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

import scrupal.storage.api.{Collection, Schema, StoreContext, WriteResult}
import scrupal.test.{FakeEntity, FakeModule, ScrupalApiSpecification}

import scala.concurrent.Await
import scala.concurrent.duration._


/** Test Specification For Instance Class */
class InstanceSpec extends ScrupalApiSpecification("InstanceSpec") {

  class TestModule(db: String) extends FakeModule('foo, db) {
    override val types = Seq(thai, buns)
    override val entities = Seq(plun)
    val thai = StringType('Thai, "Thai Foon", ".*".r)
    val buns = BundleType('Buns, "Buns Aye", Map("tie" -> thai))
    val plun = FakeEntity("Plun", buns)
  }

  "Module Type, Entity and Instance " should {
    "support CRUD" in {
      testScrupal.withStoreContext { sc : StoreContext ⇒
        implicit val ec = sc.ec
        val future = sc.ensureSchema(ApiSchemaDesign()) flatMap { schema: Schema ⇒
          schema.withCollection("instances") { coll: Collection[Instance] ⇒
            val foo = new TestModule(specName)
            foo.id must beEqualTo('foo)
            val instance = Instance("Instance", "Instigating Instance", foo.buns, foo.plun.id, Map("tie" → "foo"))
            coll.insert(instance) flatMap { wr: WriteResult ⇒
              wr.isFailure must beFalse
              coll.fetch(instance.getPrimaryId()) map { oi2: Option[Instance] ⇒
                oi2.isDefined must beTrue
                val i2 = oi2.get
                i2.getPrimaryId() must beEqualTo(instance.getPrimaryId())
                i2.name must beEqualTo("Instance")
                i2.entityId must beEqualTo('Plun)
              }
            }
          }
        }
        Await.result(future, 2.seconds)
      }
      // FIXME: Do Update and Delete too!
    }
  }}
