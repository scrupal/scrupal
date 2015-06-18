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

package scrupal.store.reactivemongo

import reactivemongo.api.DefaultDB
import reactivemongo.bson._

import scala.concurrent.{Future, Await}

/** DataAccessObject Test Suite */
class DataAccessObjectSpec extends DBContextSpecification("DataAccessObjectSpec") {

  sequential

  case class TestData(_id: Long, int: Int, str: String, list: List[String]) extends Storable[Long]

  case class TestDao(db: DefaultDB, collectionName: String) extends DataAccessObject[TestData,Long] {
    val reader = Macros.reader[TestData]
    val writer = Macros.writer[TestData]
    val converter = (id: Long) ⇒ BSONLong(id)
  }

  case class TestData2(_id: BSONObjectID = BSONObjectID.generate, int: Int = (Math.random*1000).toInt)
      extends Storable[BSONObjectID]

  case class TestDao2(db: DefaultDB, collectionName: String) extends DataAccessObject[TestData2,BSONObjectID] {
    val reader = Macros.reader[TestData2]
    val writer = Macros.writer[TestData2]
    val converter = (id: BSONObjectID) ⇒ id
  }


  "DataAccessObject" should {
    "create new collection" in {
      withEmptyDB("test_newCollection") { db ⇒
        val dao = new TestDao(db, "testdata")
        val future = dao.ensureIndices.map { list ⇒ list.exists( p ⇒ p) }
        Await.result(future, timeout) must beTrue
      }
    }

    "drop collections" in {
      withEmptyDB("test_dropCollection") { db ⇒
        val dao = new TestDao(db, "testdata")
        val future  = dao.ensureIndices.flatMap { list ⇒
          list.exists(p ⇒ p) must beTrue
          dao.drop.flatMap { u ⇒ db.hasCollection("testdata") }
        }
        Await.result(future, timeout) must beFalse
      }
    }

    "use generated BSONObjectId" in {
      withEmptyDB("test_generatedBSONObjectID") { db ⇒
        val dao = new TestDao2(db, "testdata")
        val td2 = TestData2()
        val future = dao.insert(td2) flatMap { wr ⇒
          wr.ok must beTrue
          dao.fetch(td2._id) map { optTD ⇒
            optTD match {
              case Some(td) ⇒
                td._id must beEqualTo(td2._id)
                td.int must beEqualTo(td2.int)
                success
              case None ⇒ failure("not found")
            }
          }
        }
        Await.result(future, timeout)
      }
    }

    "insert and find items" in {
      withEmptyDB("test_insertFind") { db ⇒
        val dao = new TestDao(db, "testdata")
        val future = dao.ensureIndices.flatMap { list ⇒
          list.exists(p ⇒ p) must beTrue
          val i1 = dao.insert(TestData(1,42,"42",List()))
          val i2 = dao.insert(TestData(2,43,"43",List("1")))
          val i3 = dao.insert(TestData(3,44,"44",List("2")))
          val inserts = Future sequence List(i1,i2,i3)
          val fetched = inserts flatMap { list ⇒
            list.exists { wc ⇒ wc.hasErrors must beFalse }
            val f1 = dao.fetch(1)
            val f2 = dao.fetch(2)
            val f3 = dao.fetch(3)
            Future sequence List(f1,f2,f3)
          }
          fetched.map { options ⇒
            val objs = for (o ← options) yield {
              o.isDefined must beTrue
              o.get
            }
            objs.exists { x ⇒ x._id == 1 } must beTrue
            objs.exists { x ⇒ x._id == 2 } must beTrue
            objs.exists { x ⇒ x._id == 3 } must beTrue
          }
        }
        Await.result(future, timeout)
      }
    }
  }
}
