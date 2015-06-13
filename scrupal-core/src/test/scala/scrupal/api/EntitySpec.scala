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

import org.joda.time.DateTime
import reactivemongo.api.DefaultDB
import reactivemongo.bson.Macros
import scrupal.db._
import scrupal.test.ScrupalSpecification

import scala.concurrent.Await

import BSONHandlers._

/**
 * Test that our basic abstractions for accessing the database hold water.
 */
case class SomeValue(x: Short, y: Short)

object SomeValue {
  implicit val SomeValueHandler = Macros.handler[SomeValue]
}

case class TestEntity(
  _id : Identifier,
  name: String,
  description: String,
  testVal : SomeValue,
  modified : Option[DateTime] = None,
  created : Option[DateTime] = None
) extends Storable[Identifier] with Nameable with Describable with Modifiable {
}

object TestEntity {
  case class TestEntityDao(db: DefaultDB) extends IdentifierDAO[TestEntity] {
    final val collectionName = "test_entities"
    implicit val reader = Macros.reader[TestEntity]
    implicit val writer = Macros.writer[TestEntity]
  }
}

class TestSchema(dbc: DBContext) extends scrupal.db.Schema(dbc, "TestSchema") {

  import TestEntity._

  val test_entities = withDB { db => new TestEntityDao(db) }

  def daos : Seq[DataAccessInterface[_,_]] = {
    Seq( test_entities )
  }

  def validateDao(dao: DataAccessInterface[_,_]) : Boolean = true
}

class EntitySpec extends ScrupalSpecification("EntitySpec")
{
	val te =  TestEntity('Test, "Test", "This is a test", SomeValue(1,2), None, None)

	"Entity" should {
		"fail to compare against a non-entity" in {
			val other = "not-matchable"
			te.equals(other) must beFalse
			te.equals(te) must beTrue
		}
    "save, load and delete from DB" in {
      withDBContext { context : DBContext =>
        val ts = new TestSchema(context)
        val future = ts.create(context) flatMap { cr1 =>
          for (r <- cr1) { r._2 must beTrue }
          ts.test_entities.upsert(te) flatMap { te1 =>
            te1.isDefined must beTrue
            ts.test_entities.fetch(te._id) flatMap { te3 =>
              te3.isDefined must beTrue
              val te4 = te3.get
              te4._id must beEqualTo(te._id)
              te4.testVal.equals(te.testVal) must beTrue
              val te5 = TestEntity(te4._id, "Test", "This is a test", SomeValue(2, 3), None, None)
              te4.testVal.equals(te5.testVal) must beFalse
              te5._id must beEqualTo(te4._id)
              ts.test_entities.upsert(te5) flatMap { te6 =>
                te6.isDefined must beTrue
                te6.get._id must beEqualTo(te5._id)
                ts.test_entities.removeById(te5._id) map { result =>
                  result.hasErrors must beFalse
                }
              }
            }
          }
        }
        Await.result(future, timeout * 2)
      }
    }
  }
}
