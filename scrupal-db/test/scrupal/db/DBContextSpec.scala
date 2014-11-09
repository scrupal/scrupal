package scrupal.db

import org.specs2.mutable.Specification
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONString, BSONDocument}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, duration}

/** Test Cases For DBContext
 */
class DBContextSpec extends Specification() {

  sequential

  val one_second = Duration(1,duration.SECONDS)

  "DBContext" should {
    "startup and shutdown only once" in {
      DBContext.startup()
      DBContext.startup()
      DBContext.numberOfStartups must beEqualTo(2)
      DBContext.isStartedUp must beTrue
      DBContext.shutdown()
      DBContext.numberOfStartups must beEqualTo(1)
      DBContext.isStartedUp must beTrue
      DBContext.shutdown()
      DBContext.numberOfStartups must beEqualTo(0)
      DBContext.isStartedUp must beFalse
    }

    "ensure withEmptyDB gives us an empty one" in {
      new FakeDBContext("ensure_empty") {
        val future = withEmptyDB("test_empty") { db =>
          db.isEmpty
        }
        Await.result(future, timeout) must beTrue
      }
      DBContext.numberOfStartups must beEqualTo(0)
    }

    "drop new or existing database" in {
      new FakeDBContext("create_and_drop") {
        val future = withDB("test_dropDB") { db =>
          db.drop()
        }
        Await.result(future, timeout)
        success
      }
      DBContext.numberOfStartups must beEqualTo(0)
    }

    "empty an existing database" in {
      new FakeDBContext("test_emptyDatabase") {
        withEmptyDB("text_emptyDatabase") { implicit db =>
          val f1 = db.isEmpty
          val f2 = {
            val coll = db.collection[BSONCollection]("foo")
            coll.insert(BSONDocument("_id" -> BSONString("foo"))).map { le => true}
          }
          val f3 = db.emptyDatabase.map { x => x.count { p => p._2} == x.size}
          val f4 = db.isEmpty
          val f = Future sequence List(f1, f2, f3, f4)
          val list = Await.result(f, one_second)
          for (r <- list) r must beTrue
        }
      }
      DBContext.numberOfStartups must beEqualTo(0)
    }
  }
}
