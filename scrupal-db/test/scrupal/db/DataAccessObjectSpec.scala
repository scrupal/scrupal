package scrupal.db

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
    val converter = (id: Long) => BSONLong(id)
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
