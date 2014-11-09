package scrupal.db

import org.specs2.execute.{AsResult, Result}
import reactivemongo.api.DefaultDB

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.ExecutionContext.Implicits.global


/** A Fake DB Context used for testing
 * Created by reidspencer on 10/21/14.
 */
abstract class FakeDBContext(val contextName: String, val timeout: FiniteDuration = Duration(5,"seconds")) extends scala.AnyRef
  with org.specs2.mutable.Around with org.specs2.specification.Scope
{
  var dbContext : Option[DBContext] = None

  override def around[T: AsResult](t: => T): Result = {

    DBContext.startup()
    val uri = "mongodb://localhost:27017/"
    val dbc = DBContext.fromURI(Symbol(contextName), uri)
    dbContext = Some(dbc)
    try {
      AsResult.effectively(t)
    }
    finally {
      dbc.close()
      DBContext.shutdown()
      dbContext = None
    }
  }

  def withDBContext[T]( f: (DBContext) => T ) : T = {
    assert(dbContext.isDefined)
    val dbc = dbContext.get
    f(dbc)
  }

  def withDB[T](dbName: String) ( f : (DefaultDB) ⇒ T) : T = {
    assert(dbContext.isDefined)
    val dbc = dbContext.get
    dbc.withDatabase(dbName) { implicit db => f(db) }
  }

  def withEmptyDB[T](dbName: String)( f : (ScrupalDB) => T) : T = {
    assert(dbContext.isDefined)
    val dbc = dbContext.get
    dbc.withDatabase(dbName) { implicit db =>
      val future = db.emptyDatabase.map { emptyResults =>
        f(db)
      }
      Await.result(future, timeout)
    }
  }

}
