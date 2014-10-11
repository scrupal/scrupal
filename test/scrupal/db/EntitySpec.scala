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

package scrupal.db

import org.specs2.mutable.Specification
import play.api.libs.json.Json
import reactivemongo.api.{DB, DefaultDB}
import reactivemongo.bson.BSONObjectID
import reactivemongo.extensions.json.dao.JsonDao
import scrupal.api._
import scrupal.fakes.{WithFakeScrupal}
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Test that our basic abstractions for accessing the database hold water.
 */
case class SomeValue(x: Short, y: Short)

object SomeValue {
  implicit val SomeValue_Format = Json.format[SomeValue]
}

case class TestEntity(
  override val name: Symbol,
  override val description: String,
  testVal : SomeValue,
  override val modified : Option[DateTime] = None,
  override val created : Option[DateTime] = None,
  override val id : Identifier
) extends Thing {
}

object TestEntity {
  implicit val TestEntity_Format = Json.format[TestEntity]
}

class TestSchema(dbc: DBContext) extends Schema(dbc) {
  case class TestEntityDao(db: DB) extends JsonDao[TestEntity,BSONObjectID](db,"test_entities") with DataAccessObject[TestEntity]

  val test_entities = dbc.withDatabase { db => new TestEntityDao(db) }

  def daos : Seq[JsonDao[_,BSONObjectID] with DataAccessObject[_]] = {
    Seq( test_entities )
  }

  def validateDao(dao: JsonDao[_,BSONObjectID] with DataAccessObject[_]) : Boolean = true
}

class EntitySpec extends Specification
{
	val te =  TestEntity('Test, "This is a test", SomeValue(1,2), None, None, 'Test)

	"Entity" should {
		"fail to compare against a non-entity" in {
			val other = "not-matchable"
			te.equals(other) must beFalse
			te.equals(te) must beTrue
		}
    "save, load and delete from DB" in new WithFakeScrupal {
      withDBContext { implicit context : DBContext =>
        val ts = new TestSchema(context)
        ts.create(context)
        val te2 = ts.test_entities.upsertSync(te)
        val te3 = ts.test_entities.fetchSync(te.id)
        te3.isDefined must beTrue
        val te4 = te3.get
        te4.id must beEqualTo(te.id)
        te4.testVal.equals(te4.testVal) must beTrue
        val te5 = TestEntity('Test, "This is a test", SomeValue(2,3), None, None, te4.id)
        ts.test_entities.upsert(te5)
        te4.testVal.equals(te5.testVal) must beFalse
        te5.id must beEqualTo(te4.id)
        // FIXME: ts.test_entities.delete(te5) must beTrue
      }
    }
  }
}
