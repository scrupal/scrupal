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

    "create DB From URI and drop it" in {
      new WithFakeDB("test_fromURI") {
        withDBContext { dbc ⇒
          val dbc = DBContext.fromURI('test_fromURI1, "mongodb://localhost/test_fromURI")
          Await.result(dbc.isEmpty, one_second) must beTrue
          dbc.withDatabase { db => db.drop()}
          success
        }
      }
      DBContext.numberOfStartups must beEqualTo(0)
    }

    "empty an existing database" in {
      new WithFakeDB("test_emptyDatabase") {
        withDBContext { dbc ⇒
          val f1 = dbc.isEmpty
          val f2 = dbc.withDatabase { db =>
            val coll = db.collection[BSONCollection]("foo")
            coll.insert(BSONDocument("_id" -> BSONString("foo"))).map { le => true}
          }
          val f3 = dbc.emptyDatabase().map { x => x.count { p => p._2} == x.size}
          val f4 = dbc.isEmpty
          val f = Future sequence List(f1, f2, f3, f4)
          val list = Await.result(f, one_second)
          for (r <- list) r must beTrue
        }
      }
      DBContext.numberOfStartups must beEqualTo(0)
    }
  }
}
