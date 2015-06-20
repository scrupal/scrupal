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

import scrupal.api.types.{BundleType, StringType}
import scrupal.test.{FakeEntity, FakeModule, ScrupalApiSpecification}

/**
 * Created by reid on 11/11/14.
 */
class InstanceSpec extends ScrupalApiSpecification("InstanceSpec") {

  class TestModule(db: String) extends FakeModule('foo, db) {
    val thai = StringType('Thai, "Thai Foon", ".*".r)
    val buns = BundleType[Any]('Buns, "Buns Aye", Map("tie" -> thai))
    val plun = FakeEntity("Plun", buns)

    override val types = Seq(thai, buns)

    override val entities = Seq(plun)
  }

  "Module Type, Entity and Instance " should {
    "support CRUD" in { pending("Revision")
      /** TODO: Reinstate test case for Instances
      withEmptyDB(specName) { db =>
        withSchema { schema : Schema =>
          val foo = new TestModule(specName)

          foo.id must beEqualTo('foo)

          /*        val ty_id = schema.types.insert()
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
  */
          val instance = Instance('Inst, "Instance", "Instigating Instance", foo.plun.id, BSONDocument())
          val wr = schema.instances.insertSync(instance)
          wr.hasErrors must beFalse
          val oi2 = schema.instances.fetchSync('Inst)
          oi2.isDefined must beTrue
          val i2 = oi2.get
          i2._id must beEqualTo('Inst)
          i2.name must beEqualTo("Instance")
          i2.entityId must beEqualTo('Plun)

          // FIXME: Do Update and Delete too!
        }
      } */
    }
  }}
