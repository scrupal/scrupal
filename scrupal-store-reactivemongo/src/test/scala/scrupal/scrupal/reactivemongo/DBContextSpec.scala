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

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONString, BSONDocument}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, duration}

/** Test Cases For DBContext
 */
class DBContextSpec extends DBContextSpecification("DBContextSpec") {

  sequential

  lazy val one_second = Duration(1,duration.SECONDS)

  "DBContext" should {

    "drop new or existing database" in {
      withDB("test_dropDB") { db ⇒
        val future = db.drop().map { _ ⇒ false }
        // FIXME: This should really check if the db is gone after the drop() future completes
        Await.result(future, timeout) must beFalse
      }
    }

    "ensure withEmptyDB gives us an empty one" in {
      withEmptyDB("test_empty") { db ⇒
        val future = db.isEmpty
        Await.result(future, timeout) must beTrue
      }
    }

    "empty an existing database" in {
      withEmptyDB("text_emptyDatabase") { implicit db ⇒
        val future = db.isEmpty.flatMap { didEmpty ⇒
          didEmpty must beTrue
          val coll = db.collection[BSONCollection]("foo")
          coll.insert(BSONDocument("_id" -> BSONString("foo"))).map { le ⇒ true} flatMap { truth ⇒
            truth must beTrue
            db.emptyDatabase.map { x ⇒ x.count { p ⇒ p._2} == x.size} flatMap { truth ⇒
              truth must beTrue
              db.isEmpty
            }
          }
        }
        Await.result(future, timeout) must beTrue
      }
    }
  }
}
