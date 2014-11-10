package scrupal.db

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
      withDB("test_dropDB") { db =>
        val future = db.drop().map { _ => false }
        // FIXME: This should really check if the db is gone after the drop() future completes
        Await.result(future, timeout) must beFalse
      }
    }

    "ensure withEmptyDB gives us an empty one" in {
      withEmptyDB("test_empty") { db =>
        val future = db.isEmpty
        Await.result(future, timeout) must beTrue
      }
    }

    "empty an existing database" in {
      withEmptyDB("text_emptyDatabase") { implicit db =>
        val future = db.isEmpty.flatMap { didEmpty =>
          didEmpty must beTrue
          val coll = db.collection[BSONCollection]("foo")
          coll.insert(BSONDocument("_id" -> BSONString("foo"))).map { le => true} flatMap { truth =>
            truth must beTrue
            db.emptyDatabase.map { x => x.count { p => p._2} == x.size} flatMap { truth =>
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
