package scrupal.db

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{FailoverStrategy, MongoConnection, DefaultDB}

import scala.concurrent.{ExecutionContext, Future}

/** Scrupal Database Abstraction
  *
  * This just extends ReactiveMongo's DefaultDB with a few functions suited for Scrupal
  * Created by reid on 11/9/14.
  */
class ScrupalDB(name: String, connection: MongoConnection,
  failoverStrategy : FailoverStrategy = DefaultFailoverStrategy) extends DefaultDB(name, connection, failoverStrategy) {

  def emptyDatabase(implicit ec: ExecutionContext) : Future[List[(String, Boolean)]] = {
    collectionNames flatMap { names =>
      val futures = for (cName <- names if !cName.startsWith("system.")) yield {
        val coll = collection[BSONCollection](cName)
        coll.drop() map { b => cName -> true }
      }
      Future sequence futures
    }
  }

  def dropCollection(collName: String)(implicit ec: ExecutionContext) : Future[Boolean] = {
    collection[BSONCollection](collName).drop map { foo => true } // FIXME: Deal with result code better here
  }

  def hasCollection(collName: String)(implicit ec: ExecutionContext) : Future[Boolean] = {
    collectionNames.map { list ⇒ list.contains(collName) }
  }

  def isEmpty(implicit ec: ExecutionContext) : Future[Boolean] = {
    collectionNames.map { list =>
      val names = list.filterNot { name ⇒ name.startsWith("system.") }
      names.isEmpty
    }
  }
}
