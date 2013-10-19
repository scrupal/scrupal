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
import scala.slick.lifted.DDL
import scrupal.api._
import scala.slick.lifted
import scrupal.fakes.{WithFakeScrupal}
import org.joda.time.DateTime
import scala.slick.session.Session


/**
 * Test that our basic abstractions for accessing the database hold water.
 */
case class SomeValue(x: Short, y: Short)

case class TestEntity(
  override val name: Symbol,
  override val description: String,
  testVal : SomeValue,
  override val modified : Option[DateTime] = None,
  override val created : Option[DateTime] = None,
  override val id : Option[Identifier] = None
) extends NumericThing(name, description, modified, created, id) {
}

trait TestComponent extends Component {
  import sketch.profile.simple._

  implicit val someValueMapper = lifted.MappedTypeMapper.base[SomeValue,Int](
    { v => v.x << 16 + v.y }, { i => SomeValue((i >> 16).toShort, (i & 0x0FFFF).toShort) } )

  object TestEntities extends ScrupalTable[TestEntity]("test_entities") with NumericThingTable[TestEntity] {
    def testVal = column[SomeValue]("test_val")
    def * = name ~ description ~ testVal ~modified.? ~ created.? ~ id.? <> (TestEntity.tupled, TestEntity.unapply _)
  }
}

class TestSchema(sketch: Sketch)(implicit session: Session) extends Schema(sketch) with TestComponent {
  val ddl : DDL = TestEntities.ddl
}

class EntitySpec extends Specification
{
	val te =  TestEntity('Test, "This is a test", SomeValue(1,2))

	"Entity" should {
		"fail to compare against a non-entity" in {
			val other = "not-matchable"
			te.equals(other) must beFalse
			te.equals(te) must beTrue
		}
    "save, load and delete from DB" in new WithFakeScrupal {
      withDBSession { implicit session : Session =>
        val ts = new TestSchema(sketch)
        ts.create
        import ts._
        val te2 = TestEntities.upsert(te)
        val te3 = TestEntities.fetch(te2)
        te3.isDefined must beTrue
        val te4 = te3.get
        te4.id.get must beEqualTo(te2)
        te4.testVal.equals(te4.testVal) must beTrue
        val te5 = TestEntity('Test, "This is a test", SomeValue(2,3), None, None, te4.id)
        TestEntities.update(te5)
        te4.testVal.equals(te5.testVal) must beFalse
        te5.id must beEqualTo(te4.id)
        TestEntities.delete(te5) must beTrue
      }
    }
  }
}
