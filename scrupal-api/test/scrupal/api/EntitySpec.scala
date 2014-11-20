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
