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

package scrupal.core.api

import org.joda.time.DateTime
import org.specs2.mutable.Specification
import reactivemongo.api.{DefaultDB, DB}
import reactivemongo.bson.Macros
import scrupal.core.FakeScrupal
import scrupal.db._

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
  case class TestEntityDao(db: DefaultDB) extends DataAccessObject[TestEntity,Identifier](db,"test_entities") {
    implicit val idHandler = (id: Symbol) ⇒ reactivemongo.bson.BSONString(id.name)
    implicit val modelHandler = Macros.handler[TestEntity]
  }

}

class TestSchema(dbc: DBContext) extends Schema(dbc) {

  import TestEntity._

  val test_entities = dbc.withDatabase { db => new TestEntityDao(db) }

  def daos : Seq[DataAccessObject[_,_]] = {
    Seq( test_entities )
  }

  def validateDao(dao: DataAccessObject[_,_]) : Boolean = true
}

class EntitySpec extends Specification
{
	val te =  TestEntity('Test, "Test", "This is a test", SomeValue(1,2), None, None)

	"Entity" should {
		"fail to compare against a non-entity" in {
			val other = "not-matchable"
			te.equals(other) must beFalse
			te.equals(te) must beTrue
		}
    "save, load and delete from DB" in new FakeScrupal("test-EntitySpec") {
      withDBContext { context : DBContext =>
        val ts = new TestSchema(context)
        ts.create(context)
        val te2 = ts.test_entities.upsertSync(te)
        val te3 = ts.test_entities.fetchSync(te._id)
        te3.isDefined must beTrue
        val te4 = te3.get
        te4._id must beEqualTo(te._id)
        te4.testVal.equals(te4.testVal) must beTrue
        val te5 = TestEntity(te4._id, "Test", "This is a test", SomeValue(2,3), None, None)
        ts.test_entities.upsert(te5)
        te4.testVal.equals(te5.testVal) must beFalse
        te5._id must beEqualTo(te4._id)
        // FIXME: ts.test_entities.delete(te5) must beTrue
      }
    }
  }
}
